document.addEventListener('DOMContentLoaded', function() {

	// if (window.location.href.includes('file.html')) {
	// 	// 如果当前页面是file.html，高亮显示图标
	// 	document.querySelectorAll('.leftsidebar nav ul li img').forEach(img => img.classList.remove('active'));
	// 	fileManagementIcon.querySelector('img').classList.add('active');
	// } else if (window.location.href.includes('workstation.html')) {
	// 	document.querySelectorAll('.leftsidebar nav ul li img').forEach(img => img.classList.remove(
	// 		'active'));
	// 	dashboardIcon.querySelector('img').classList.add('active');
	// } else if (window.location.href.includes('cgroup.html')) {
	// 	document.querySelectorAll('.leftsidebar nav ul li img').forEach(img => img.classList.remove(
	// 		'active'));
	// 	cgroupIcon.querySelector('img').classList.add('active');
	// } else if (window.location.href.includes('task.html')) {
	// 	document.querySelectorAll('.leftsidebar nav ul li img').forEach(img => img.classList.remove(
	// 		'active'));
	// 	taskIcon.querySelector('img').classList.add('active');
	// }

	// 解析 URL 参数
	const urlParams = new URLSearchParams(window.location.search);
	const username = urlParams.get('username');

	if (username) {
		document.getElementById('greeting').textContent = '${username}，下午好';
	}

	// 处理侧边栏图标悬停
	const sidebarIcons = document.querySelectorAll('.leftsidebar nav ul li');

	sidebarIcons.forEach(icon => {
		const hoverTitle = document.createElement('div');
		hoverTitle.className = 'hover-title';
		hoverTitle.style.display = 'none';
		icon.appendChild(hoverTitle);

		icon.addEventListener('mouseover', function() {
			const title = this.getAttribute('data-title');
			if (title) {
				hoverTitle.textContent = title;
				hoverTitle.style.display = 'block';
			}
		});

		icon.addEventListener('mouseout', function() {
			hoverTitle.style.display = 'none';
		});
	});

	// 点击文件管理图标
	const fileManagementIcon = document.getElementById('file-management');
	fileManagementIcon.addEventListener('click', function() {
		// 高亮显示选中的图标
		document.querySelectorAll('.leftsidebar nav ul li img').forEach(img => img.classList.remove(
			'active'));
		fileManagementIcon.querySelector('img').classList.add('active');
		// 页面跳转
		window.location.href = 'file.html';
	});

	// 点击工作台图标返回原界面
	const dashboardIcon = document.getElementById('dashboard');
	dashboardIcon.addEventListener('click', function() {
		// 高亮显示选中的图标
		document.querySelectorAll('.leftsidebar nav ul li img').forEach(img => img.classList.remove(
			'active'));
		dashboardIcon.querySelector('img').classList.add('active');
		// 页面跳转
		window.location.href = 'workstation.html';
	});

	// 点击组管理图标
	const cgroupIcon = document.getElementById('cgroup-manage');
	cgroupIcon.addEventListener('click', function() {
		// 高亮显示选中的图标
		document.querySelectorAll('.leftsidebar nav ul li img').forEach(img => img.classList.remove(
			'active'));
		cgroupIcon.querySelector('img').classList.add('active');
		// 页面跳转
		window.location.href = 'cgroup.html';
	});

	// 点击任务管理图标
	const taskIcon = document.getElementById('task-manage');
	taskIcon.addEventListener('click', function() {
		// 高亮显示选中的图标
		document.querySelectorAll('.leftsidebar nav ul li img').forEach(img => img.classList.remove(
			'active'));
		taskIcon.querySelector('img').classList.add('active');
		// 页面跳转
		window.location.href = 'task.html';
	});

	// 上传文件逻辑
	const uploadButton = document.getElementById('uploadButton');
	const uploadModal = document.getElementById('uploadModal');
	const closeButton = document.querySelector('.close-button');
	const fileInput = document.getElementById('fileInput');
	const confirmUploadButton = document.getElementById('confirmUploadButton');

	uploadButton.addEventListener('click', function() {
		uploadModal.style.display = 'block';
	});

	closeButton.addEventListener('click', function() {
		uploadModal.style.display = 'none';
	});

	window.addEventListener('click', function(event) {
		if (event.target === uploadModal) {
			uploadModal.style.display = 'none';
		}
	});

	confirmUploadButton.addEventListener('click', function() {
		const file = fileInput.files[0];
		if (file) {
			const formData = new FormData();
			formData.append('file', file);

			fetch('http://192.168.81.133:8081/api/user/uploadFile', {
					method: 'POST',
					body: formData
				})
				.then(response => response.json())
				.then(data => {
					if (data.success) {
						alert('文件上传成功');
					} else {
						alert('文件上传失败: ' + data.message);
					}
					uploadModal.style.display = 'none';
				})
				.catch(error => {
					console.error('Error:', error);
					alert('文件上传时发生错误');
					uploadModal.style.display = 'none';
				});
		} else {
			alert('请先选择文件');
		}
	});
});