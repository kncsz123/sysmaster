document.addEventListener('DOMContentLoaded', function() {
	const addTaskButton = document.getElementById('add-task');
	const taskModal = document.getElementById('task-modal');
	const closeModalButton = document.getElementById('close-modal');
	const closeFormButton = document.getElementById('close-form');
	const saveAndAddButton = document.getElementById('save-and-add');
	const taskTableBody = document.getElementById('task-table-body');
	const taskDetailModal = document.getElementById('task-detail-modal');
	const closeDetailModalButton = document.getElementById('close-detail-modal');
	const taskDetailsContainer = document.getElementById('task-details');
	const migrateTaskModal = document.getElementById('migrate-task-modal');
	const closeMigrateModalButton = document.getElementById('close-migrate-modal');
	const confirmMigrateButton = document.getElementById('confirm-migrate');
	const cgroupListContainer = document.getElementById('cgroup-list');
	let selectedCgroup = null;
	let selectedTaskPid = null;

	// Fetch and display tasks
	fetch('http://192.168.81.133:8081/api/user/selectTask')
		.then(response => response.json())
		.then(data => {
			if (data.tasks) {
				data.tasks.forEach(task => {
					const row = document.createElement('tr');
					row.innerHTML = `
                        <td><input type="checkbox"></td>
                        <td>${task.name}</td>
                        <td><button class="status-btn ${task.status === 'running' ? 'run' : 'stop'}">${task.status === 'running' ? '运行' : '停止'}</button></td>
                        <td>${task.path}</td>
                        <td>${task.lastRun}</td>
                        <td>${task.nextRun}</td>
                        <td>${task.datestamp}</td>
                        <td>${task.description}</td>
                        <td>
                            <button class="edit">编辑</button> 
                            <button class="view-details" data-pid="${task.pid}">查看详情</button>
                            <button class="migrate" data-pid="${task.pid}">迁移任务</button>
                            <button class="delete" data-pid="${task.pid}">删除</button>
                        </td>
                    `;
					taskTableBody.appendChild(row);
				});
			}
		})
		.catch(error => console.error('Error fetching tasks:', error));

	// Open modal to add task
	addTaskButton.addEventListener('click', function() {
		taskModal.style.display = 'block';
	});

	// Close modals
	closeModalButton.addEventListener('click', function() {
		taskModal.style.display = 'none';
	});

	closeFormButton.addEventListener('click', function() {
		taskModal.style.display = 'none';
	});

	closeDetailModalButton.addEventListener('click', function() {
		taskDetailModal.style.display = 'none';
	});

	// Close modal when clicking outside of it
	window.addEventListener('click', function(event) {
		if (event.target == taskModal) {
			taskModal.style.display = 'none';
		}
		if (event.target == taskDetailModal) {
			taskDetailModal.style.display = 'none';
		}
		if (event.target == migrateTaskModal) {
			migrateTaskModal.style.display = 'none';
		}
	});

	// Handle save and add new task
	saveAndAddButton.addEventListener('click', function() {
		const commands = {
			name: document.getElementById('task-name').value,
			params: document.getElementById('task-params').value,
			programName: document.getElementById('program-name').value,
			status: document.querySelector('input[name="status"]:checked').value,
			cronExpression: [document.getElementById('cron-expression').value], // 将单个值包装在数组中
			expressionDesc: document.getElementById('expression-desc').value,
			className: document.getElementById('class-name').value,
			remarks: document.getElementById('remarks').value
		};

		fetch('http://192.168.81.133:8081/api/user/createTask', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'
				},
				body: JSON.stringify(commands)
			})
			.then(response => response.json())
			.then(data => {
				if (data.success) {
					alert('任务创建成功');
					// Optionally clear form fields here
				} else {
					alert('任务创建失败');
				}
			})
			.catch(error => console.error('Error creating task:', error));
	});

	// Handle delete task
	taskTableBody.addEventListener('click', function(event) {
		if (event.target.classList.contains('delete')) {
			const pid = event.target.getAttribute('data-pid');

			// 创建键值对对象
			const params = new Map();
			params.set('pid', pid);

			// 将 Map 对象转换为一个普通的对象，然后转换为 JSON 字符串
			const jsonObject = {};
			params.forEach((value, key) => {
				jsonObject[key] = value;
			});

			fetch(`http://192.168.81.133:8081/api/user/deleteTask`, {
					method: 'DELETE',
					headers: {
						'Content-Type': 'application/json'
					},
					body: JSON.stringify(jsonObject) // 传递 JSON 对象
				})
				.then(response => response.json())
				.then(data => {
					if (data.success) {
						alert('任务删除成功');
						// 从表格中移除任务行
						event.target.closest('tr').remove();
					} else {
						alert('任务删除失败');
					}
				})
				.catch(error => console.error('Error deleting task:', error));
		}

		// Handle view task details
		if (event.target.classList.contains('view-details')) {
			const pid = event.target.getAttribute('data-pid');

			fetch(`http://192.168.81.133:8081/api/user/viewTaskResourceUsage?pid=${pid}`)
				.then(response => response.json())
				.then(data => {
					taskDetailsContainer.innerHTML = `
                        <div>User: ${data.user}</div>
                        <div>SHR: ${data.shr}</div>
                        <div>RES: ${data.res}</div>
                        <div>VIRT: ${data.virt}</div>
                        <div>NI: ${data.ni}</div>
                        <div>PR: ${data.pr}</div>
                        <div>Command: ${data.command}</div>
                        <div>MEM%: ${data["mem%"]}</div>
                        <div>CPU%: ${data["cpu%"]}</div>
                        <div>S: ${data.s}</div>
                    `;
					taskDetailModal.style.display = 'block';
				})
				.catch(error => console.error('Error fetching task details:', error));
		}
	});

	// Handle migrate task
	taskTableBody.addEventListener('click', function(event) {
		if (event.target.classList.contains('migrate')) {
			const pid = event.target.getAttribute('data-pid');
			selectedTaskPid = pid;

			fetch('http://192.168.81.133:8081/api/user/selectCgroup')
				.then(response => response.json())
				.then(data => {
					cgroupListContainer.innerHTML = ''; // Clear previous cgroups
					if (data.cgroup) {
						data.cgroup.forEach(cgroup => {
							const cgroupDiv = document.createElement('div');
							cgroupDiv.innerHTML = `
                                <span>${cgroup.name} (${cgroup.cgroupPath})</span>
                                <span class="plus-button" data-path="${cgroup.cgroupPath}">+</span>
                            `;
							cgroupListContainer.appendChild(cgroupDiv);
						});

						// Attach click event to plus buttons
						document.querySelectorAll('.plus-button').forEach(button => {
							button.addEventListener('click', function() {
								document.querySelectorAll('.plus-button').forEach(
									btn => btn.classList.remove('active'));
								if (this.classList.contains('active')) {
									this.classList.remove('active');
									selectedCgroup = null;
								} else {
									this.classList.add('active');
									selectedCgroup = this.getAttribute('data-path');
								}
							});
						});

						migrateTaskModal.style.display = 'block';
					}
				})
				.catch(error => console.error('Error fetching cgroups:', error));
		}
	});

	// Close migrate modal
	closeMigrateModalButton.addEventListener('click', function() {
		migrateTaskModal.style.display = 'none';
	});

	// Handle confirm migrate
	confirmMigrateButton.addEventListener('click', function() {
		if (selectedCgroup && selectedTaskPid) {
			const payload = {
				pid: selectedTaskPid,
				target: selectedCgroup
			};

			fetch('http://192.168.81.133:8081/api/user/migrateTask', {
					method: 'PUT',
					headers: {
						'Content-Type': 'application/json'
					},
					body: JSON.stringify(payload)
				})
				.then(response => response.json())
				.then(data => {
					if (data.success) {
						alert('任务迁移成功');
						migrateTaskModal.style.display = 'none';
					} else {
						alert('任务迁移失败');
					}
				})
				.catch(error => console.error('Error migrating task:', error));
		} else {
			alert('请选择一个目标cgroup');
		}
	});

	// Close modal when clicking outside of it
	window.addEventListener('click', function(event) {
		if (event.target === migrateTaskModal) {
			migrateTaskModal.style.display = 'none';
		}
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