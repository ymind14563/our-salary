package main.main.auth.handler;

import main.main.auth.jwt.JwtTokenizer;
import main.main.auth.utils.CustomAuthorityUtils;
import main.main.member.entity.Member;
import main.main.member.service.MemberService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OAuth2MemberSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenizer jwtTokenizer;
    private final CustomAuthorityUtils authorityUtils;
    private final MemberService memberService;

    public OAuth2MemberSuccessHandler(JwtTokenizer jwtTokenizer, CustomAuthorityUtils authorityUtils, MemberService memberService) {
        this.jwtTokenizer = jwtTokenizer;
        this.authorityUtils = authorityUtils;
        this.memberService = memberService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request
            , HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        var oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = String.valueOf(oAuth2User.getAttributes().get("email"));
        String name = String.valueOf(oAuth2User.getAttributes().get("name"));
        String password = String.valueOf(oAuth2User.getAttributes().get("sub"));
        List<String> authorities = authorityUtils.createRoles(email);
        Member member;
        try {
            member = saveMember(email, name, password);
            redirect(request, response, member, authorities);
        } catch (Exception e) {
            redirect(request, response, memberService.findMember(email), authorities);
        }
    }

    private Member saveMember(String email, String name, String password) {
        Member member = new Member(email, name, password);
        return memberService.createMember(member);
    }

    private void redirect(HttpServletRequest request, HttpServletResponse response
            , Member member, List<String> authorities) throws IOException {
        String accessToken = delegateAccessToken(member, authorities);
        String refreshToken = delegateRefreshToken(member.getEmail());
        String addedAccessToken = "Bearer " + accessToken;

        response.setHeader("Authorization", addedAccessToken);
        response.setHeader("Refresh", refreshToken);

        String uri = createUri(addedAccessToken, member.getMemberId()).toString();
        getRedirectStrategy().sendRedirect(request, response, uri);
    }

    private String delegateAccessToken(Member member, List<String> authorities) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("memberId", member.getMemberId());
        claims.put("id", member.getEmail());
        claims.put("roles", authorities);

        String subject = member.getEmail();
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getAccessTokenExpirationMinutes());
        String base43EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        String accessToken = jwtTokenizer.generateAccessToken(claims, subject, expiration, base43EncodedSecretKey);

        return accessToken;
    }

    private String delegateRefreshToken(String username) {
        String subject = username;
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getRefreshTokenExpirationMinutes());
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        String refreshToken = jwtTokenizer.generateRefreshToken(subject, expiration, base64EncodedSecretKey);

        return refreshToken;
    }

    private URI createUri(String accessToken, long memberId) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("Authorization", accessToken);
        queryParams.add("MemberId", String.valueOf(memberId));

        return UriComponentsBuilder
                .newInstance()
                .scheme("http")
                .host("ourmainpro-33.s3-website.ap-northeast-2.amazonaws.com")
//                .port(3000)
//                .path("/")
                .queryParams(queryParams)
                .build()
                .toUri();
    }
}
