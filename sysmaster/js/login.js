document.addEventListener('DOMContentLoaded', function() {
	const loginForm = document.getElementById('loginForm');

	// 保存用户输入的用户名和密码
	const usernameInput = document.getElementById('username');
	const passwordInput = document.getElementById('password');

	// 检查本地存储中是否有保存的用户名和密码
	const savedUsername = localStorage.getItem('username');
	const savedPassword = localStorage.getItem('password');

	if (savedUsername && savedPassword) {
		usernameInput.value = savedUsername;
		passwordInput.value = savedPassword;
	}

	loginForm.addEventListener('submit', function(event) {
		event.preventDefault();

		const username = usernameInput.value;
		const password = passwordInput.value;

		// 将用户名和密码保存到本地存储中
		localStorage.setItem('username', username);
		localStorage.setItem('password', password);

		// 创建登录请求的参数
		const loginData = {
			username: username,
			password: password
		};

		// 发送登录请求到本地服务器
		fetch('http://192.168.81.133:8081/api/user/selectUserInfo', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'
				},
				body: JSON.stringify(loginData)
			})
			.then(response => response.json())
			.then(data => {
				console.log('Login response:', data);
				if (data.success) {
					alert('登录成功');
					// 跳转到工作台页面并带上用户名参数
					window.location.href =
						'../workstation.html?username=${encodeURIComponent(username)}';
				} else {
					alert('登录失败: ' + data.message);
				}
			})
			.catch(error => {
				console.error('Error:', error);
				alert('登录时发生错误');
			});
	});
});