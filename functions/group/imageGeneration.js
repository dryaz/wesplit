const axios = require("axios");
const { v4: uuidv4 } = require("uuid");
const { onDocumentWritten } = require("firebase-functions/v2/firestore");
const { getRemoteConfig } = require("firebase-admin/remote-config");
const { defineSecret } = require("firebase-functions/params");
const { admin } = require("../services/firebase");

const OPENAI_API_KEY = defineSecret("OPENAI_API_KEY");
const STORAGE_BUCKET = "wesplit-bill.appspot.com";

const GPT_IMAGE_QUALITIES = new Set(["low", "medium", "high", "auto"]);

/** Map legacy DALL·E quality values to GPT Image equivalents. */
function normalizeQuality(quality) {
  if (!quality) return "medium";
  const normalized = quality.toLowerCase();
  if (GPT_IMAGE_QUALITIES.has(normalized)) return normalized;
  if (normalized === "hd") return "high";
  if (normalized === "standard") return "medium";
  return "medium";
}

/** Prefer gpt-image models; map retired DALL·E ids. */
function normalizeModel(model) {
  if (!model) return "gpt-image-2";
  const normalized = model.toLowerCase();
  if (normalized === "dall-e-2" || normalized === "dall-e-3") {
    return "gpt-image-2";
  }
  return model;
}

exports.handleImageGeneration = onDocumentWritten(
  {
    document: "groups/{groupId}",
    region: "us-central1",
    secrets: [OPENAI_API_KEY],
  },
  async (event) => {
    const beforeData = event.data.before ? event.data.before.data() : null;
    const afterData = event.data.after ? event.data.after.data() : null;
    const groupId = event.params.groupId;

    if (!afterData) {
      return;
    }

    const newImageDescription = afterData.imageDescription;
    const oldImageDescription = beforeData ? beforeData.imageDescription : null;

    if (newImageDescription === oldImageDescription || !newImageDescription) {
      return;
    }

    try {
      const template = await getRemoteConfig(admin.app()).getServerTemplate();
      const config = template.evaluate();

      const imageResolution = config.getString("image_res") || "1024x1024";
      const imageQuality = normalizeQuality(config.getString("image_quality"));
      const imageModel = normalizeModel(config.getString("image_model"));
      const imagePromptTemplate =
        config.getString("image_prompt") ||
        'Generate a square flat vector-style icon about "{description}". The icon should look visually appealing and recognizable even at 128x128 resolution. No borders, paddings, or text.';

      console.log(
        `Generating image for group: ${groupId} | ${newImageDescription} | ${imageModel}, ${imageQuality}, ${imageResolution}`
      );

      const prompt = imagePromptTemplate.replace("{description}", newImageDescription);

      // GPT Image models return b64_json (no temporary URL / no `style` param).
      const openaiResponse = await axios.post(
        "https://api.openai.com/v1/images/generations",
        {
          prompt,
          n: 1,
          model: imageModel,
          quality: imageQuality,
          size: imageResolution,
        },
        {
          headers: {
            Authorization: `Bearer ${OPENAI_API_KEY.value()}`,
            "Content-Type": "application/json",
          },
        }
      );

      const imageData = openaiResponse.data?.data?.[0];
      if (!imageData?.b64_json && !imageData?.url) {
        throw new Error(
          `OpenAI returned no image data: ${JSON.stringify(openaiResponse.data)}`
        );
      }

      let imageBuffer;
      if (imageData.b64_json) {
        imageBuffer = Buffer.from(imageData.b64_json, "base64");
      } else {
        const imageResponse = await axios.get(imageData.url, {
          responseType: "arraybuffer",
        });
        imageBuffer = Buffer.from(imageResponse.data);
      }

      const timestamp = Date.now();
      const fileName = `${groupId}/${timestamp}.png`;
      const bucket = admin.storage().bucket(STORAGE_BUCKET);
      const file = bucket.file(fileName);
      const downloadToken = uuidv4();

      await file.save(imageBuffer, {
        metadata: {
          contentType: "image/png",
          metadata: {
            firebaseStorageDownloadTokens: downloadToken,
          },
        },
      });

      const publicUrl = `https://firebasestorage.googleapis.com/v0/b/${STORAGE_BUCKET}/o/${encodeURIComponent(
        fileName
      )}?alt=media&token=${downloadToken}`;

      console.log(`Image uploaded successfully: ${publicUrl}`);

      await event.data.after.ref.update({
        imageUrl: publicUrl,
        isImageGen: false,
      });

      console.log(`Updated Firestore document for group: ${groupId}`);
    } catch (error) {
      console.error(
        `Error generating or uploading image for group: ${groupId}`,
        error.response?.data || error.message
      );

      try {
        await event.data.after.ref.update({ isImageGen: false });
      } catch (updateError) {
        console.error(
          `Failed to clear isImageGen for group: ${groupId}`,
          updateError.message
        );
      }
    }
  }
);
