document.addEventListener('DOMContentLoaded', function() {
	// 页面加载时查询接口数据
	fetch('http://192.168.81.133:8081/api/user/selectFile')
		.then(response => {
			if (!response.ok) {
				throw new Error('网络请求错误');
			}
			return response.json();
		})
		.then(data => {
			if (data.status === 400) {
				throw new Error(data.message || '服务器错误');
			}

			const files = data.files;

			// 获取文件列表表格的 tbody 元素
			const fileListTbody = document.querySelector('.file-list tbody');

			// 遍历数据，创建表格行
			files.forEach(file => {
				// 创建一个新的表格行
				const row = document.createElement('tr');

				// 创建每个单元格并赋值
				const nameCell = document.createElement('td');
				nameCell.textContent = file.name;

				const modifiedCell = document.createElement('td');
				modifiedCell.textContent = file.lastmodified;

				// 将所有单元格添加到行中
				row.appendChild(nameCell);
				row.appendChild(modifiedCell);

				// 将行添加到表格主体
				fileListTbody.appendChild(row);
			});
		})
		.catch(error => {
			console.error('获取数据时发生错误:', error);
		});

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
});