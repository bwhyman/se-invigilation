package com.se.invigilation.dto;

import com.se.invigilation.dox.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserDTO {
    private String collId;
    private String collegeName;
    private List<User> users;
}
