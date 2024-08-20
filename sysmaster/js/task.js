document.addEventListener('DOMContentLoaded', function() {
	// 处理分页
	let currentPage = 1;
	let totalPages = 0;
	let taskData = [];
	let totalItems = 0;
	const pageSizeSelect = document.getElementById('page-size');
	const prevPageButton = document.getElementById('prev-page');
	const nextPageButton = document.getElementById('next-page');
	const currentPageSpan = document.getElementById('currentPage');

	// 搜索任务
	const taskNameInput = document.getElementById('task-name');
	const startTimeInput = document.getElementById('start-time');
	const endTimeInput = document.getElementById('end-time');
	const filterTasksButton = document.getElementById('search-task');

	// 查询任务详情
	const taskTableBody = document.getElementById('task-table-body');
	const taskDetailModal = document.getElementById('task-detail-modal');
	const closeDetailModalButton = document.getElementById('close-detail-modal');
	const taskDetailsContainer = document.getElementById('task-details');

	// 迁移任务
	const migrateTaskModal = document.getElementById('migrate-task-modal');
	const closeMigrateModalButton = document.getElementById('close-migrate-modal');
	const confirmMigrateButton = document.getElementById('confirm-migrate');
	const cgroupListContainer = document.getElementById('cgroup-list');

	// Variables to store selected cgroup and task PID
	let selectedCgroup = null;
	let selectedTaskPid = null;
	let selectedCgroupHierarchy = null;

	// Fetch and display tasks with pagination
	function fetchTasks(page = 1, pageSize = 10) {
		// 每次查询清除原来样式
		taskData = [];
		totalItems = 0;
		
		fetch(`http://192.168.81.134:8081/api/user/selectTask?page=${page}&pageSize=${pageSize}`)
			.then(response => response.json())
			.then(data => {
				if(data.status !== 200){
					const errorMessage = document.createElement('div');
					errorMessage.className = 'error-message';
					errorMessage.textContent = '查询任务失败';
					document.body.appendChild(errorMessage);
					setTimeout(() => errorMessage.remove(), 3000);
				}
				if (data.tasks) {
					taskData = taskData.concat(data.tasks);
					totalItems += data.tasks.length;
					taskTableBody.innerHTML = ''; // Clear existing rows
					taskData.forEach(task => {
						const row = document.createElement('tr');
						row.innerHTML = `
                            <td><input type="checkbox"></td>
                            <td>${task.name}</td>
                            <td><button class="status-btn ${task.status === true ? 'stop' : 'run'}">${task.status === true ? '停止' : '运行'}</button></td>
                            <td>${task.path}</td>
                            <td>${task.totalTimeOfRecentRun}</td>
                            <td>${task.datestamp}</td>
                            <td>${task.description}</td>
                            <td>
                                <button class="view-details" data-pid="${task.pid}">查看详情</button>
                                <button class="migrate" data-pid="${task.pid}">迁移任务</button>
                                <button class="delete" data-pid="${task.pid}">删除</button>
                            </td>
                        `;
						taskTableBody.appendChild(row);
					});
					// Update pagination info
					totalPages = Math.ceil(totalItems / pageSizeSelect.value);
					updatePagination();
				}
			})
			.catch(error => console.error('Error fetching tasks:', error));
	}

	// Select All Checkbox Functionality
	const selectAllCheckbox = document.getElementById('select-all');
	const taskCheckboxes = document.querySelectorAll('#task-table-body input[type="checkbox"]');

	// Select All Checkbox Functionality
	selectAllCheckbox.addEventListener('change', function() {
		const taskCheckboxes = document.querySelectorAll('#task-table-body input[type="checkbox"]');
		taskCheckboxes.forEach(checkbox => checkbox.checked = selectAllCheckbox.checked);
	});

	// Event listeners for modal open/close
	closeDetailModalButton.addEventListener('click', () => taskDetailModal.style.display = 'none');
	closeMigrateModalButton.addEventListener('click', () => migrateTaskModal.style.display = 'none');

	// Close modal when clicking outside of it
	window.addEventListener('click', function(event) {
		if (event.target === taskDetailModal) taskDetailModal.style.display = 'none';
		if (event.target === migrateTaskModal) migrateTaskModal.style.display = 'none';
	});

	// Handle task actions (delete, view details, migrate, change status)
	taskTableBody.addEventListener('click', function(event) {
		const target = event.target;

		// Handle task status change
		if (target.classList.contains('status-btn')) {
			const pid = target.closest('tr').querySelector('.view-details').getAttribute('data-pid');
			const isRunning = target.classList.contains('stop'); // True if currently running

			const payload = {
				pid: pid,
				open: !
					isRunning // Toggle the status: if running, set to false; if stopped, set to true
			};

			fetch('http://192.168.81.134:8081/api/user/changeTaskStatus', {
					method: 'PUT',
					headers: {
						'Content-Type': 'application/json'
					},
					body: JSON.stringify(payload)
				})
				.then(response => response.json())
				.then(data => {
					if(data === null){
						throw new Error('Request failed with status ');
					}
					if(data.status === 200){
						if (isRunning) {
							target.classList.remove('stop');
							target.classList.add('run');
							target.textContent = '运行';
						} else {
							target.classList.remove('run');
							target.classList.add('stop');
							target.textContent = '停止';
						}
						const successMessage = document.createElement('div');
						successMessage.className = 'success-message';
						successMessage.textContent = '任务状态更新成功';
						document.body.appendChild(successMessage);
						setTimeout(() => successMessage.remove(), 3000);
					} else if(data.status === 400){
						const errorMessage = document.createElement('div');
						errorMessage.className = 'error-message';
						errorMessage.textContent = '更新过程出现错误';
						document.body.appendChild(errorMessage);
						setTimeout(() => errorMessage.remove(), 3000);
					} else if(data.status === 500){
						const errorMessage = document.createElement('div');
						errorMessage.className = 'error-message';
						errorMessage.textContent = '服务器错误';
						document.body.appendChild(errorMessage);
						setTimeout(() => errorMessage.remove(), 3000);
					}
				})
				.catch(error => console.error('Error changing task status:', error));
		}
	});

	// Handle task actions (delete, view details, migrate)
	taskTableBody.addEventListener('click', function(event) {
		const target = event.target;

		// Handle task deletion
		if (target.classList.contains('delete')) {
			const pid = target.getAttribute('data-pid');
			const params = JSON.stringify({
				pid: pid
			});

			fetch(`http://192.168.81.134:8081/api/user/deleteTask`, {
					method: 'DELETE',
					headers: {
						'Content-Type': 'application/json'
					},
					body: params
				})
				.then(response => response.json())
				.then(data => {
					if(data === null){
						throw new Error('Request failed with status ');
					}
					if(data.status === 200){
						const successMessage = document.createElement('div');
						successMessage.className = 'success-message';
						successMessage.textContent = '任务删除成功';
						document.body.appendChild(successMessage);
						setTimeout(() => successMessage.remove(), 3000);
					} else if(data.status === 404){
						const errorMessage = document.createElement('div');
						errorMessage.className = 'error-message';
						errorMessage.textContent = '任务不存在';
						document.body.appendChild(errorMessage);
						setTimeout(() => errorMessage.remove(), 3000);
					} else if(data.status === 400){
						const errorMessage = document.createElement('div');
						errorMessage.className = 'error-message';
						errorMessage.textContent = '删除异常';
						document.body.appendChild(errorMessage);
						setTimeout(() => errorMessage.remove(), 3000);
					} else if(data.status === 404){
						const errorMessage = document.createElement('div');
						errorMessage.className = 'error-message';
						errorMessage.textContent = '服务器错误';
						document.body.appendChild(errorMessage);
						setTimeout(() => errorMessage.remove(), 3000);
					}
				})
				.catch(error => console.error('Error deleting task:', error));
		}

		// Handle viewing task details
		if (target.classList.contains('view-details')) {
			const pid = target.getAttribute('data-pid');

			fetch(`http://192.168.81.134:8081/api/user/viewTaskResourceUsage?pid=${pid}`)
				.then(response => response.json())
				.then(data => {
					if(data === null){
						throw new Error('Request failed with status ');
					}
					if(data.status === 200){
						taskDetailsContainer.innerHTML = `
						    <div>所属用户: ${data.user}</div>
						    <div>共享内存: ${data.shr}KB</div>
						    <div>常驻内存: ${data.res}KB</div>
						    <div>虚拟内存: ${data.virt}KB</div>
						    <div>优先级: ${data.ni}</div>
						    <div>调度优先级: ${data.pr}</div>
						    <div>创建命令: ${data.command}</div>
						    <div>内存使用率: ${data["mem%"]}</div>
						    <div>CPU使用率: ${data["cpu%"]}</div>
						    <div>运行状态: ${data.s}</div>
						`;
						taskDetailModal.style.display = 'block';
					} else if(data.status === 400) {
						const errorMessage = document.createElement('div');
						errorMessage.className = 'error-message';
						errorMessage.textContent = '查询出现错误';
						document.body.appendChild(errorMessage);
						setTimeout(() => errorMessage.remove(), 3000);
					} else if(data.status === 500){
						const errorMessage = document.createElement('div');
						errorMessage.className = 'error-message';
						errorMessage.textContent = '服务器错误';
						document.body.appendChild(errorMessage);
						setTimeout(() => errorMessage.remove(), 3000);
					}
					
				})
				.catch(error => console.error('Error fetching task details:', error));
		}

		// Handle migrating task
		if (target.classList.contains('migrate')) {
			const pid = target.getAttribute('data-pid');
			selectedTaskPid = pid;

			fetch('http://192.168.81.134:8081/api/user/selectCgroup')
				.then(response => response.json())
				.then(data => {
					if(data === null){
						throw new Error('Request failed with status ');
					}
					if(data.status === 200){
						cgroupListContainer.innerHTML = ''; // Clear previous cgroups
						if (data.cgroups) {
							data.cgroups.forEach(cgroup => {
								const cgroupDiv = document.createElement('div');
								cgroupDiv.innerHTML = `
						            <span>${cgroup.name} (${cgroup.cgroupPath})</span>
						            <span class="plus-button" data-name="${cgroup.name}" data-hierarchy="${cgroup.hierarchy}">+</span>
						        `;
								cgroupListContainer.appendChild(cgroupDiv);
							});
						
							// Attach click event to plus buttons
							document.querySelectorAll('.plus-button').forEach(button => {
								button.addEventListener('click', function() {
									document.querySelectorAll('.plus-button').forEach(
										btn => btn.classList.remove('active'));
									this.classList.toggle('active');
									selectedCgroup = this.classList.contains('active') ?
										this.getAttribute('data-name') : null;
									selectedCgroupHierarchy = this.classList.contains('active') ? 
										this.getAttribute('data-hierarchy') : null;
								});
							});
						
							migrateTaskModal.style.display = 'block';
						}
					} else if(data.status === 400) {
						const errorMessage = document.createElement('div');
						errorMessage.className = 'error-message';
						errorMessage.textContent = '查询出现错误';
						document.body.appendChild(errorMessage);
						setTimeout(() => errorMessage.remove(), 3000);
					} else if(data.status === 500){
						const errorMessage = document.createElement('div');
						errorMessage.className = 'error-message';
						errorMessage.textContent = '服务器错误';
						document.body.appendChild(errorMessage);
						setTimeout(() => errorMessage.remove(), 3000);
					}
				})
				.catch(error => console.error('Error fetching cgroups:', error));
		}
	});

	// Confirm task migration
	confirmMigrateButton.addEventListener('click', function() {
		if (selectedCgroup && selectedTaskPid) {
			const payload = {
				pid: selectedTaskPid,
				target: selectedCgroup,
				hierarchy: selectedCgroupHierarchy,
			};

			fetch('http://192.168.81.134:8081/api/user/migrateTask', {
					method: 'PUT',
					headers: {
						'Content-Type': 'application/json'
					},
					body: JSON.stringify(payload)
				})
				.then(response => response.json())
				.then(data => {
					if(data === null){
						throw new Error('Request failed with status ');
					}
					
					if(data.status === 200){
						const successMessage = document.createElement('div');
						successMessage.className = 'success-message';
						successMessage.textContent = '任务迁移成功';
						document.body.appendChild(successMessage);
						setTimeout(() => successMessage.remove(), 3000);
					} else if(data.status === 400){
						const errorMessage = document.createElement('div');
						errorMessage.className = 'error-message';
						errorMessage.textContent = '请求错误';
						document.body.appendChild(errorMessage);
						setTimeout(() => errorMessage.remove(), 3000);
					} else if(data.status === 500){
						const errorMessage = document.createElement('div');
						errorMessage.className = 'error-message';
						errorMessage.textContent = '服务器错误';
						document.body.appendChild(errorMessage);
						setTimeout(() => errorMessage.remove(), 3000);
					}
				})
				.catch(error => console.error('Error migrating task:', error));
		} else {
			const errorMessage = document.createElement('div');
			errorMessage.className = 'error-message';
			errorMessage.textContent = '请选择一个目标组';
			document.body.appendChild(errorMessage);
			setTimeout(() => errorMessage.remove(), 3000);
		}
	});

	// Sidebar Icon Hover Handling
	document.querySelectorAll('.leftsidebar nav ul li').forEach(icon => {
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

		icon.addEventListener('mouseout', () => hoverTitle.style.display = 'none');
	});

	// Sidebar Navigation Handling
	const handleNavigation = (elementId, targetPage) => {
		const icon = document.getElementById(elementId);
		icon.addEventListener('click', function() {
			document.querySelectorAll('.leftsidebar nav ul li img').forEach(img => img.classList
				.remove('active'));
			icon.querySelector('img').classList.add('active');
			window.location.href = targetPage;
		});
	};

	handleNavigation('file-management', 'file.html');
	handleNavigation('dashboard', 'workstation.html');
	handleNavigation('cgroup-manage', 'cgroup.html');
	handleNavigation('task-manage', 'task.html');

	//=======================================================================
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
	fileManagementIcon.addEventListener(
		'click',
		function() {
			document.querySelectorAll('.leftsidebar nav ul li img').forEach(img => img.classList.remove(
				'active'));
			fileManagementIcon.querySelector('img').classList.add('active');
			window.location.href = 'file.html';
		});

	// 点击工作台图标返回原界面
	const dashboardIcon = document.getElementById('dashboard');
	dashboardIcon.addEventListener('click', function() {
		document.querySelectorAll('.leftsidebar nav ul li img').forEach(img => img.classList.remove(
			'active'));
		dashboardIcon.querySelector('img').classList.add('active');
		window.location.href = 'workstation.html';
	});

	// 点击组管理图标
	const cgroupIcon = document.getElementById('cgroup-manage');
	cgroupIcon.addEventListener('click', function() {
		document.querySelectorAll('.leftsidebar nav ul li img').forEach(img => img.classList.remove(
			'active'));
		cgroupIcon.querySelector('img').classList.add('active');
		window.location.href = 'cgroup.html';
	});

	// 点击任务管理图标
	const taskIcon = document.getElementById('task-manage');
	taskIcon.addEventListener('click', function() {
		document.querySelectorAll('.leftsidebar nav ul li img').forEach(img => img.classList.remove(
			'active'));
		taskIcon.querySelector('img').classList.add('active');
		window.location.href = 'task.html';
	});
	//================================================================

	// 处理分页
	function updatePagination() {
		currentPageSpan.textContent = `${currentPage}/${totalPages}`;
		prevPageButton.disabled = currentPage <= 1;
		nextPageButton.disabled = currentPage >= totalPages;
	}

	// Handle page size change
	pageSizeSelect.addEventListener('change', function() {
		const pageSize = parseInt(this.value);
		fetchTasks(1, pageSize); // Fetch first page with new page size
	});

	// Handle previous page button click
	prevPageButton.addEventListener('click', function() {
		if (currentPage > 1) {
			fetchTasks(currentPage - 1, pageSizeSelect.value);
		}
	});

	// Handle next page button click
	nextPageButton.addEventListener('click', function() {
		if (currentPage < totalPages) {
			fetchTasks(currentPage + 1, pageSizeSelect.value);
		}
	});

	// Fetch initial data
	fetchTasks(currentPage, pageSizeSelect.value);

	// 搜索任务
	function showDatePicker(inputElement) {
		const datepicker = document.createElement('input');
		datepicker.type = 'date';
		datepicker.style.position = 'absolute';
		datepicker.style.left = `${inputElement.offsetLeft}px`;
		datepicker.style.top = `${inputElement.offsetTop + inputElement.offsetHeight}px`;
		datepicker.style.zIndex = 1000;

		datepicker.addEventListener('change', function() {
			const selectedDate = new Date(this.value);
			const formattedDate =
				`${selectedDate.getFullYear()}年${selectedDate.getMonth() + 1}月${selectedDate.getDate()}日`;
			inputElement.value = formattedDate;
			document.body.removeChild(datepicker);
		});

		document.body.appendChild(datepicker);
		datepicker.focus();
	}

	document.getElementById('start-time-picker').addEventListener('click', function() {
		showDatePicker(startTimeInput);
	});

	document.getElementById('end-time-picker').addEventListener('click', function() {
		showDatePicker(endTimeInput);
	});

	// Handle Filter Tasks Button Click
	filterTasksButton.addEventListener('click', function() {
		const taskName = taskNameInput.value || null;
		const startTime = startTimeInput.value || null;
		const endTime = endTimeInput.value || null;

		const params = {
			name: taskName,
			startTime: startTime,
			endTime: endTime
		};

		// Send the parameters to the backend
		fetch('http://192.168.81.134:8081/api/user/filterTasks', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'
				},
				body: JSON.stringify(params)
			})
			.then(response => response.json())
			.then(data => {
				if(data === null){
					throw new Error('Request failed with status ');
				}
				
				if(data.status === 200){
					console.log('Filtered tasks:', data);
					const successMessage = document.createElement('div');
					successMessage.className = 'success-message';
					successMessage.textContent = '筛选成功';
					document.body.appendChild(successMessage);
					setTimeout(() => successMessage.remove(), 3000); 
				} else if(data.status === 400){
					const errorMessage = document.createElement('div');
					errorMessage.className = 'error-message';
					errorMessage.textContent = '请求错误';
					document.body.appendChild(errorMessage);
					setTimeout(() => errorMessage.remove(), 3000);
				} else if(data.status === 500){
					const errorMessage = document.createElement('div');
					errorMessage.className = 'error-message';
					errorMessage.textContent = '服务器错误';
					document.body.appendChild(errorMessage);
					setTimeout(() => errorMessage.remove(), 3000);
				}
			})
			.catch(error => console.error('Error filtering tasks:', error));
	});
	
	// 成功提示
	function showSuccessMesssage() {
		
	}
	// 失败提示
});