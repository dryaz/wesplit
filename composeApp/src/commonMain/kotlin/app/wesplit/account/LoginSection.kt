package app.wesplit.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import app.wesplit.ShareData
import app.wesplit.ShareDelegate
import app.wesplit.domain.model.account.Login
import app.wesplit.isDebugEnvironment
import app.wesplit.ui.OrDivider
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.andspaces
import split.composeapp.generated.resources.debug_anonymous_login
import split.composeapp.generated.resources.img_login
import split.composeapp.generated.resources.join_agree
import split.composeapp.generated.resources.login_button_cd
import split.composeapp.generated.resources.login_to_create_descr
import split.composeapp.generated.resources.privacy_policy
import split.composeapp.generated.resources.terms_conditions

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
internal fun LoginSection(
    modifier: Modifier,
    onLoginRequest: (Login) -> Unit,
) {
    val windowSizeClass = calculateWindowSizeClass()
    Column(
        modifier = modifier.fillMaxSize(1f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (windowSizeClass.heightSizeClass != WindowHeightSizeClass.Compact) {
            Image(
                modifier = Modifier,
                painter = painterResource(Res.drawable.img_login),
                contentDescription = stringResource(Res.string.login_button_cd),
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(Res.string.login_to_create_descr),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(32.dp))
        GoogleLoginButton { onLoginRequest(Login.Social(Login.Social.Type.GOOGLE)) }
        Spacer(modifier = Modifier.height(8.dp))
        OrDivider()
        Spacer(modifier = Modifier.height(8.dp))
        AppleLoginButton { onLoginRequest(Login.Social(Login.Social.Type.APPLE)) }
        
        // Debug button for localhost/development
        if (isDebugEnvironment()) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onLoginRequest(Login.Anonymous) },
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            ) {
                Text(stringResource(Res.string.debug_anonymous_login))
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        TermsAndPolicyText(modifier = Modifier.padding(horizontal = 32.dp).alpha(0.65f))
    }
}

@Composable
private fun TermsAndPolicyText(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val shareDelegate: ShareDelegate = koinInject()
    val privacy = stringResource(Res.string.privacy_policy)
    val terms = stringResource(Res.string.terms_conditions)

    val annotatedString =
        if (privacy.isNullOrBlank() || terms.isNullOrBlank()) {
            buildAnnotatedString {}
        } else {
            buildAnnotatedString {
                append(stringResource(Res.string.join_agree))
                withLink(
                    link =
                        LinkAnnotation
                            .Clickable(
                                tag = privacy,
                                styles =
                                    TextLinkStyles(
                                        style =
                                            SpanStyle(
                                                color = MaterialTheme.colorScheme.primary,
                                                textDecoration = TextDecoration.Underline,
                                            ),
                                    ),
                                linkInteractionListener = {
                                    if (shareDelegate.supportPlatformSharing()) {
                                        shareDelegate.open(ShareData.Link("https://wesplit.app/privacypolicy/"))
                                    } else {
                                        uriHandler.openUri("https://wesplit.app/privacypolicy/")
                                    }
                                },
                            ),
                ) {
                    append(privacy)
                }
                append(stringResource(Res.string.andspaces))
                withLink(
                    link =
                        LinkAnnotation
                            .Clickable(
                                tag = terms,
                                styles =
                                    TextLinkStyles(
                                        style =
                                            SpanStyle(
                                                color = MaterialTheme.colorScheme.primary,
                                                textDecoration = TextDecoration.Underline,
                                            ),
                                    ),
                                linkInteractionListener = {
                                    if (shareDelegate.supportPlatformSharing()) {
                                        shareDelegate.open(ShareData.Link("https://wesplit.app/terms/"))
                                    } else {
                                        uriHandler.openUri("https://wesplit.app/terms/")
                                    }
                                },
                            ),
                ) {
                    append(terms)
                }
            }
        }

    Text(
        modifier = modifier,
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
    )
}
