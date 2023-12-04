package com.hz.api.admin.web.app;

import com.hz.api.admin.common.Result.ResultInfo;
import com.hz.api.admin.model.request.UserLoginRequest;
import com.hz.api.admin.model.request.UserPasswordRequest;
import com.hz.api.admin.web.service.UserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/password/list")
    public ResultInfo getUserPassword(@RequestParam("pageSize") int pageSize,
                                      @RequestParam("pageNum") int pageNum){
        return ResultInfo.success(userService.getUserPassword(pageSize, pageNum));
    }

    @PostMapping("/password/save")
    public ResultInfo saveUserPassword(@RequestBody @Validated UserPasswordRequest userPasswordRequest){
        userService.addUserPassword(userPasswordRequest);
        return ResultInfo.success();
    }

    @PostMapping("/password/delete")
    public ResultInfo deleteUserPassword(@RequestParam("ids") List<Long> ids){
        userService.deleteUserPassword(ids);
        return ResultInfo.success();
    }

    @PostMapping("/login")
    public ResultInfo userLogin(@RequestBody UserLoginRequest userLoginRequest){
        return ResultInfo.success(userService.userLogin(userLoginRequest));
    }

    @GetMapping("/menu")
    public ResultInfo getUserMenu(){
        return ResultInfo.success(userService.getUserMenu());
    }

    @GetMapping("/permission")
    public ResultInfo getPermission(){
        String[] permission = {"sys:user:add",
                "sys:user:edit",
                "sys:user:delete",
                "sys:user:import",
                "sys:user:export"};

        return ResultInfo.success(Arrays.stream(permission).toArray());
    }
}
