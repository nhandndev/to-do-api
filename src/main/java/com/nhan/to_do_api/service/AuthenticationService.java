package com.nhan.to_do_api.service;

import com.nhan.to_do_api.dto.request.AuthenticationRequest;
import com.nhan.to_do_api.dto.request.RegisterRequest;
import com.nhan.to_do_api.dto.response.AuthenticationResponse;
import com.nhan.to_do_api.dto.response.UserResponse;
import com.nhan.to_do_api.entity.User;
import com.nhan.to_do_api.enums.Role;
import com.nhan.to_do_api.exception.AppException;
import com.nhan.to_do_api.exception.ErrorCode;
import com.nhan.to_do_api.mapper.UserMapper;
import com.nhan.to_do_api.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
public class AuthenticationService {
    @Value("${jwt.signerKey}")
    private String SIGN_KEY;
   @Autowired
   private UserRepository userRepository;
   @Autowired
   private PasswordEncoder passwordEncoder;
   @Autowired
   private UserMapper userMapper;
   public UserResponse register(RegisterRequest registerRequest) {
       if(userRepository.existsByUsername(registerRequest.getUsername())) {
           throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
       }
       if(userRepository.existsByEmail(registerRequest.getEmail())) {
           throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
       }
       User user = userMapper.toUser(registerRequest);
       user.setPassword(passwordEncoder.encode(user.getPassword()));
       user.setRole(Role.USER);
       user.setCreatedAt(LocalDateTime.now());
       user.setUpdatedAt(LocalDateTime.now());
       user = userRepository.save(user);
       return userMapper.toUserResponse(user);
   }
   public AuthenticationResponse login(AuthenticationRequest authenticationRequest) {
       var user = userRepository.findByUsername(authenticationRequest.getUsername()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
       boolean authenticated = passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword());
       if(!authenticated) {
           throw new AppException(ErrorCode.UNAUTHORIZED);
       }
       String token = generateToken(user);
       return AuthenticationResponse.builder()
               .authenticated(true)
               .token(token)
               .build();


   }
    public String generateToken(User user) {
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build();
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().subject(user.getUsername()).issuer("DoanNgocNhan")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(9999, ChronoUnit.SECONDS).toEpochMilli()))
                .build();
//                .jwtID(UUID.randomUUID().toString()).claim("scope", buildScope(user)).build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(SIGN_KEY));
        } catch (JOSEException e) {
            throw new AppException(ErrorCode.TOKEN_CANNOT_CREATE);
        }
        return jwsObject.serialize();
    }
//    private String buildScope(User user) {
//        StringJoiner stringJoiner = new StringJoiner(" ");
//
//        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
//            user.getRoles().forEach(role -> {
//                stringJoiner.add("ROLE_" + role.getName());
//
//                if (role.getPermissions() != null && !role.getPermissions().isEmpty()) {
//                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
//                }
//            });
//        }

//        return stringJoiner.toString();
//    }
    public SignedJWT verifyToken(String token, boolean isRefresh) throws ParseException, JOSEException {
        JWSVerifier verifier = new MACVerifier(SIGN_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date ExpirationDate = (isRefresh)
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant()
                .plus(9999, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();
        boolean verified = signedJWT.verify(verifier);
        if (!(verified && ExpirationDate.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
//        if (invalidatedTokenRepository.findById(signedJWT.getJWTClaimsSet().getJWTID()).isPresent()) {
//            throw new AppException(ErrorCode.UNAUTHORIZED);
//        }
        return signedJWT;
    }
//    public AuthenticationResponse refreshToken(RefreshRequest request) throws JOSEException, ParseException {
//        var signedJWT = verifyToken(request.getToken(), false);
//        String jwtid = signedJWT.getJWTClaimsSet().getJWTID();
//        String userName = signedJWT.getJWTClaimsSet().getSubject();
//        Date ExpirationDate = signedJWT.getJWTClaimsSet().getExpirationTime();
//        InvalidatedToken invalidatedToken = InvalidatedToken.builder().id(jwtid).expiryDate(ExpirationDate).build();
//        invalidatedTokenRepository.save(invalidatedToken);
//        User user = userRepository.findByUsername(userName)
//                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
//        var token = generateToken(user);
//        return AuthenticationResponse.builder().token(token).authenticated(true).build();
//
//    }
}