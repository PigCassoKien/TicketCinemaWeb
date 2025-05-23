package com.example.besrc.Controller;

import java.io.IOException;

import com.example.besrc.Entities.VerificationCode;
import com.example.besrc.Exception.NotFoundException;
import com.example.besrc.Repository.VerificationCodeRepository;
import com.example.besrc.ServerResponse.AuthenticationResponse;
import com.example.besrc.ServerResponse.ErrorResponse;
import com.example.besrc.ServerResponse.MyApiResponse;
import com.example.besrc.Service.AuthenticationService;
import com.example.besrc.Service.EmailService;
import com.example.besrc.requestClient.LoginRequest;
import com.example.besrc.requestClient.RefreshAccessTokenRequest;
import com.example.besrc.requestClient.SignUpRequest;
import com.example.besrc.utils.VerificationCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = " Authentication Endpoint")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @PostMapping("/signup")
    @Operation(summary = "Create a new account", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully Signed Up!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MyApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Email/Username is existed or Bad password", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
    })
    public ResponseEntity<MyApiResponse> signup(@RequestBody @Valid SignUpRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new MyApiResponse("Validation failed"));
        }
        return ResponseEntity.ok(authenticationService.signUp(request, "127.0.0.1"));
    }

    @PostMapping("/admin/login")
    @Operation(summary = "Login an admin account", responses = {
            @ApiResponse(responseCode = "200", description = "Login successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "403", description = "Username or password is wrong", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
    })
    public ResponseEntity<AuthenticationResponse> adminLogin(@RequestBody @Valid LoginRequest loginrequest,
                                                             HttpServletRequest servletRequest) {
        return ResponseEntity.ok(authenticationService.logIn(loginrequest, servletRequest, true));
    }

    @PostMapping("/login")
    @Operation(summary = "Login a normal account", responses = {
            @ApiResponse(responseCode = "200", description = "Login successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "403", description = "Username or password is wrong", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
    })
    public ResponseEntity<AuthenticationResponse> login(@RequestBody @Valid LoginRequest loginrequest,
                                                        HttpServletRequest servletRequest) {
        return ResponseEntity.ok(authenticationService.logIn(loginrequest, servletRequest, false));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Service", responses = {
            @ApiResponse(responseCode = "200", description = "Get new access token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "403", description = "Refresh token is wrong", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
    })
    public ResponseEntity<AuthenticationResponse> refresh(@RequestBody @Valid RefreshAccessTokenRequest request,
                                                          HttpServletRequest servletRequest) {
        return ResponseEntity.ok(authenticationService.refreshAccessToken(request.getRefreshToken(), servletRequest));
    }

    @GetMapping("/verify/{email}/{code}")
    @Operation(summary = "Verify Account by Verifying Code (This code is sent via user's mail)", responses = {
            @ApiResponse(responseCode = "302", description = "Verified successfully, then redirect to login page.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Verified fail", content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotFoundException.class))),
    })
    public ResponseEntity<?> verify(@PathVariable String email, @PathVariable String code, HttpServletResponse response) {
        return authenticationService.verifyEmail(email, code, response);
    }

    @GetMapping("/token")
    @Operation(hidden = true)
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<MyApiResponse> getMe() {
        return ResponseEntity.ok(new MyApiResponse("ok"));
    }


    @PostMapping("/password/reset/request")
    @Operation(summary = "Request Password Reset", responses = {
            @ApiResponse(responseCode = "200", description = "Request successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MyApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Email is not existed", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
    })
    public ResponseEntity<MyApiResponse > requestPasswordReset(@RequestParam String email) throws IOException {
        return ResponseEntity.ok(authenticationService.requestPasswordReset(email));
    }


    @PostMapping("/password/reset/verify")
    @Operation(summary = "Verify Password Reset", responses = {
            @ApiResponse(responseCode = "200", description = "Verify successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MyApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Email is not existed", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
    })
    public ResponseEntity<MyApiResponse> verifyPasswordResetOtp(@RequestParam String email, @RequestParam String code) throws IOException {
        return ResponseEntity.ok(authenticationService.verifyPasswordResetOtp(email, code));
    }

    @PostMapping("/password/reset")
    @Operation(summary = "Reset Password", responses = {
            @ApiResponse(responseCode = "200", description = "Reset successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MyApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Email is not existed", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
    })
    public ResponseEntity<MyApiResponse> resetPassword(@RequestParam String email, @RequestParam String code, @RequestParam String newPassword) {
        return ResponseEntity.ok(authenticationService.resetPassword(email, code, newPassword));

    }

    @PostMapping("/verify/resend")
    @Operation(summary = "Resend Verification Code", responses = {
            @ApiResponse(responseCode = "200", description = "Resend successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MyApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Email is not existed", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
    })
    public ResponseEntity<MyApiResponse> resendOtp(@RequestParam String email, @RequestParam String purpose) throws IOException {
        return ResponseEntity.ok(authenticationService.resendVerificationEmail(email, purpose));
    }
}