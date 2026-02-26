package com.notification.service;

import com.notification.model.entity.UserPreference;
import com.notification.model.enums.ChannelType;
import com.notification.repository.UserPreferenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPreferenceServiceImplTest {

    @Mock
    private UserPreferenceRepository preferenceRepository;

    @InjectMocks
    private UserPreferenceServiceImpl preferenceService;

    @Test
    void shouldNotThrowIfChannelEnabled() {

        UserPreference preference = new UserPreference();
        preference.setEnabled(true);

        when(preferenceRepository.findByUserIdAndChannelType(1L, ChannelType.EMAIL))
                .thenReturn(Optional.of(preference));

        preferenceService.validateChannelEnabled(1L, ChannelType.EMAIL);

        // no exception expected
    }

    @Test
    void shouldThrowIfChannelDisabled() {

        UserPreference preference = new UserPreference();
        preference.setEnabled(false);

        when(preferenceRepository.findByUserIdAndChannelType(1L, ChannelType.EMAIL))
                .thenReturn(Optional.of(preference));

        assertThatThrownBy(() ->
                preferenceService.validateChannelEnabled(1L, ChannelType.EMAIL))
                .isInstanceOf(IllegalStateException.class);
    }
}
