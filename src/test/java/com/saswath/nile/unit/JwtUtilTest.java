package com.saswath.nile.unit;

import com.saswath.nile.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtUtil — Unit Tests")
class JwtUtilTest {

    private static final String SECRET = "supersecretandsecurejwtsecurtykeywhichisatleast32characterslong";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET);
    }

    @Test
    @DisplayName("generateToken returns a non-null, non-blank JWT")
    void generateToken_returnsNonBlankToken() {
        String token = jwtUtil.generateToken("user@nile.com");
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("extractEmail returns the email that was embedded in the token")
    void extractEmail_returnsCorrectEmail() {
        String email = "user@nile.com";
        String token = jwtUtil.generateToken(email);
        assertThat(jwtUtil.extractEmail(token)).isEqualTo(email);
    }

    @Test
    @DisplayName("extractEmail correctly handles emails with special characters")
    void extractEmail_handlesSpecialCharacters() {
        String email = "user+tag@sub.domain.com";
        String token = jwtUtil.generateToken(email);
        assertThat(jwtUtil.extractEmail(token)).isEqualTo(email);
    }

    @Test
    @DisplayName("isValid returns true when token matches the email")
    void isValid_returnsTrueForMatchingEmail() {
        String email = "admin@nile.com";
        String token = jwtUtil.generateToken(email);
        assertThat(jwtUtil.isValid(token, email)).isTrue();
    }

    @Test
    @DisplayName("isValid returns false when token email does not match supplied email")
    void isValid_returnsFalseForWrongEmail() {
        String token = jwtUtil.generateToken("user@nile.com");
        assertThat(jwtUtil.isValid(token, "other@nile.com")).isFalse();
    }

    @Test
    @DisplayName("isValid throws when the token is tampered with")
    void isValid_throwsOnTamperedToken() {
        String token = jwtUtil.generateToken("user@nile.com");
        String tampered = token.substring(0, token.length() - 4) + "XXXX";
        assertThatException().isThrownBy(() -> jwtUtil.isValid(tampered, "user@nile.com"));
    }

    @Test
    @DisplayName("Two tokens generated for the same email are both valid")
    void generateToken_multipleTokensForSameEmailAreValid() {
        String email = "user@nile.com";
        String t1 = jwtUtil.generateToken(email);
        String t2 = jwtUtil.generateToken(email);
        assertThat(jwtUtil.isValid(t1, email)).isTrue();
        assertThat(jwtUtil.isValid(t2, email)).isTrue();
    }
}