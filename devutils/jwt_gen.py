import jwt
import pyperclip
import time

# Your credentials
key_id = 'G27N7N267Z'
team_id = '69a6de7f-9ba8-47e3-e053-5b8c7c11a4d1'
app_bundle_id = 'app.wesplit.ios'
private_key = """-----BEGIN PRIVATE KEY-----
MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQgG1USlT7QUW4/U7fs
JFq9kuAFVPTRaA1Ifo6WUcIz83SgCgYIKoZIzj0DAQehRANCAARR8OuiP+JVqWbu
RpZfQtycohga5ik84xQ2plPmhHQnFGGAgnpl1zKXIg7josEei2fT+evTVAQXwK9j
XUTJoGA9
-----END PRIVATE KEY-----"""

# Generate JWT
current_time = int(time.time())
token = jwt.encode(
    {
        "iss": team_id,
        "iat": current_time,
        "exp": current_time + 3600,
        "aud": "appstoreconnect-v1",
        "sub": app_bundle_id
    },
    private_key,
    algorithm="ES256",
    headers={
      "alg": "ES256",
      "typ": "JWT",
      "kid": key_id
    }
)

# Create the curl command
curl_command = f"""curl -v \\
  -H "Authorization: Bearer {token}" \\
  "https://api.appstoreconnect.apple.com/v1/apps"
"""

# Copy the curl command to the clipboard
pyperclip.copy(curl_command)

# Print a confirmation message
print("Curl command copied to clipboard:")
print(curl_command)
