package cn.tedu.store.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cn.tedu.store.controller.ex.FileEmptyException;
import cn.tedu.store.controller.ex.FileIOException;
import cn.tedu.store.controller.ex.FileSizeException;
import cn.tedu.store.controller.ex.FileStateException;
import cn.tedu.store.controller.ex.FileTypeException;
import cn.tedu.store.entity.User;
import cn.tedu.store.service.IUserService;
import cn.tedu.store.util.JsonResult;

@RestController
@RequestMapping("users")
public class UserController extends BaseController {
	
	@Autowired
	private IUserService userService;
	
	@PostMapping("reg")
	public JsonResult<Void> reg(User user)  {
		userService.reg(user);
		return new JsonResult<>(SUCCESS);
	}

	@RequestMapping("login")
	public JsonResult<User> login(String username,
			String password, HttpSession session){
		// 调用业务层对象执行登录
		User data = userService.login(username, password);
		// 将uid和username存入到session中
		session.setAttribute("uid", data.getUid());
		session.setAttribute("username", data.getUsername());
		// 响应结果
		return new JsonResult<>(SUCCESS, data);
	}
	
	@RequestMapping("change_info")
	public JsonResult<Void> changeInfo(User user, HttpSession session) {
		// 从session中获取uid和username
		Integer uid = getUidFromSession(session);
		String username = getUsernameFromSession(session);
		// 调用业务层对象执行修改用户资料
		userService.changeInfo(uid, username, user);
		// 返回
		return new JsonResult<>(SUCCESS);
	}
	
	/**
	 * 上传的头像的最大大小
	 */
	public static final int AVATAR_MAX_SIZE = 700 * 1024;
	/**
	 * 允许上传的头像文件的类型
	 */
	public static final List<String> AVATAR_CONTENT_TYPES = new ArrayList<>();
	
	static {
		AVATAR_CONTENT_TYPES.add("image/jpeg");
		AVATAR_CONTENT_TYPES.add("image/png");
	}
	
	@PostMapping("change_avatar")
	public JsonResult<String> changeAvatar(
		@RequestParam("file") MultipartFile file, 
		HttpSession session) {
		// 判断用户上传是否上传了头像文件，或头像文件是否有效
		if (file.isEmpty()) {
			throw new FileEmptyException("请选择需要上传的头像文件，并且，不可以使用空文件");
		}
		
		// 判断头像文件的大小是否超标
		if (file.getSize() > AVATAR_MAX_SIZE) {
			throw new FileSizeException(
				"不可以使用超过" + AVATAR_MAX_SIZE / 1024 + "KB的头像文件");
		}
		
		// 判断上传的文件类型是否超标
		if (!AVATAR_CONTENT_TYPES.contains(file.getContentType()) ) {
			throw new FileTypeException(
				"不支持上传" + file.getContentType() + "类型的文件作为头像！允许上传的类型有：" + AVATAR_CONTENT_TYPES);
		}
		
		// 保存头像文件的文件夹的名称
		String dir = "upload";
		// 确定保存头像文件的文件夹的路径
		String pathname = session.getServletContext().getRealPath(dir);
		// 保存头像文件的文件夹
		File parent = new File(pathname);
		// 确保文件夹是存在的
		if (!parent.exists()) {
			parent.mkdirs();
		}
		
		// 获取上传的头像文件的原始文件名
		String originalFilename = file.getOriginalFilename();
		// 处理扩展名
		String suffix = "";
		int beginIndex = originalFilename.lastIndexOf(".");
		if (beginIndex > 0) {
			suffix = originalFilename.substring(beginIndex);
		}
		// 文件名
		String filename = System.currentTimeMillis() + "" + System.nanoTime() % 100000 + suffix;
		
		// 保存头像的目标文件，即：将用户上传的头像保存为哪个文件
		File dest = new File(parent, filename);
		// 保存用户上传的头像文件
		try {
			file.transferTo(dest);
		} catch (IllegalStateException e) {
			throw new FileStateException(
				"文件可能已经被移动，无法访问文件的数据");
		} catch (IOException e) {
			throw new FileIOException(
				"读写数据时出现错误");
		}
		
		// 将头像更新到数据库中
		Integer uid = getUidFromSession(session);
		String username = getUsernameFromSession(session);
		String avatar = "/upload/" + filename;
		userService.changeAvatar(uid, username, avatar);
		
		// 返回
		return new JsonResult<>(SUCCESS, avatar);
	}
	
	@PostMapping("change_password")
	public JsonResult<Void> changePassword(
		@RequestParam("old_password") String oldPassword, 
		@RequestParam("new_password") String newPassword, 
		HttpSession session) {
		// 从session中获取uid和username
		Integer uid = getUidFromSession(session);
		String username = getUsernameFromSession(session);
		// 调用业务层对象执行修改密码
		userService.changePassword(uid, username, oldPassword, newPassword);
		// 返回成功
		return new JsonResult<>(SUCCESS);
	}
	
	@GetMapping("get_by_uid")
	public JsonResult<User> getByUid(HttpSession session) {
		// 从session中获取uid
		Integer uid = getUidFromSession(session);
		// 调用业务层对象获取数据
		User data = userService.getByUid(uid);
		// 返回成功及获取到的数据
		return new JsonResult<>(SUCCESS, data);
	}
	
}





