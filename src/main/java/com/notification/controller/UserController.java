package com.notification.controller;

import com.notification.model.dto.request.AddChannelEndpointRequest;
import com.notification.model.dto.request.CreateUserRequest;
import com.notification.model.dto.request.UpdatePreferenceRequest;
import com.notification.model.dto.response.ChannelEndpointResponse;
import com.notification.model.dto.response.UserResponse;
import com.notification.model.enums.ChannelType;
import com.notification.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}/channels")
    public ResponseEntity<ChannelEndpointResponse> addChannelEndpoint(
            @PathVariable Long userId,
            @Valid @RequestBody AddChannelEndpointRequest request
    ) {
        ChannelEndpointResponse response = userService.addChannelEndpoint(userId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}/preferences/{channelType}")
    public ResponseEntity<Void> updatePreference(
            @PathVariable Long userId,
            @PathVariable ChannelType channelType,
            @Valid @RequestBody UpdatePreferenceRequest request
    ) {

        userService.updateUserPreference(
                userId,
                channelType,
                request.getEnabled()
        );

        return ResponseEntity.noContent().build();
    }
}
