package app.wesplit.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import app.wesplit.domain.model.account.Login
import app.wesplit.ui.OrDivider
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.img_login
import split.composeapp.generated.resources.login_button_cd
import split.composeapp.generated.resources.login_to_create_descr

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
        Spacer(modifier = Modifier.height(4.dp))
        OrDivider()
        Spacer(modifier = Modifier.height(4.dp))
        AnonymousLoginButton { onLoginRequest(Login.Anonymous) }
        Spacer(modifier = Modifier.height(32.dp))
        TermsAndPolicyText(modifier = Modifier.padding(horizontal = 32.dp).alpha(0.65f))
    }
}

@Composable
private fun TermsAndPolicyText(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val privacy = "Privacy Policy"
    val terms = "Terms&Conditions"

    val annotatedString =
        buildAnnotatedString {
            append("By joining, you agree to the ")
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
                                uriHandler.openUri("https://wesplit.app/privacypolicy/")
                            },
                        ),
            ) {
                append(privacy)
            }
            append(" and ")
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
                                uriHandler.openUri("https://wesplit.app/terms/")
                            },
                        ),
            ) {
                append(terms)
            }
        }

    Text(
        modifier = modifier,
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
    )
}
