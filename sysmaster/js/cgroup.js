document.addEventListener('DOMContentLoaded', function() {
	// 初始加载cgroup
	loadCgroup('/sys/fs/cgroup');

	// 加载指定路径下的子组
	function loadCgroup(cgroupPath, indent = 0, parentRow = null) {
		fetch(
				`http://192.168.81.134:8081/api/user/selectChildCgroup?cgroupPath=${encodeURIComponent(cgroupPath)}`
			)
			.then(response => response.json())
			.then(data => {
				if (data === null) {
					throw new Error('Request failed with status ');
				}

				if (data) {
					data.cgroups.forEach(cgroup => {
						// 创建行元素
						const row = document.createElement('tr');
						row.setAttribute('data-path', cgroupPath + '/' + cgroup.name);

						// 创建箭头单元格并正确应用缩进
						const arrowCell = document.createElement('td');
						arrowCell.style.paddingLeft = `${indent}px`;
						const arrow = document.createElement('span');
						arrow.textContent = '►';
						arrow.style.cursor = 'pointer';
						arrow.style.paddingRight = '10px';

						arrow.addEventListener('mouseover', function() {
							arrow.style.color = '#007bff';
						});

						arrow.addEventListener('mouseout', function() {
							arrow.style.color = 'black';
						});

						arrow.addEventListener('click', function() {
							if (arrow.textContent === '►') {
								arrow.textContent = '▼';
								const path = cgroupPath + '/' + cgroup.name;
								loadCgroup(path, indent + 20, row); // 递增缩进
							} else {
								arrow.textContent = '►';
								removeSubGroups(cgroupPath + '/' + cgroup.name); // 传递完整路径
							}
						});

						arrowCell.appendChild(arrow);
						arrowCell.appendChild(document.createTextNode(cgroup.name));
						row.appendChild(arrowCell);

						// 创建其他信息单元格
						const hierarchyCell = document.createElement('td');
						hierarchyCell.textContent = cgroup.hierarchy;
						row.appendChild(hierarchyCell);

						const datestampCell = document.createElement('td');
						datestampCell.textContent = cgroup.datestamp;
						row.appendChild(datestampCell);

						const descriptionCell = document.createElement('td');
						descriptionCell.textContent = cgroup.description;
						row.appendChild(descriptionCell);

						// 创建操作按钮单元格
						const actionCell = document.createElement('td');

						// 查看组配置按钮
						const viewButton = document.createElement('button');
						viewButton.textContent = '查看组配置';
						viewButton.className = 'button';
						viewButton.addEventListener('click', function() {
							viewGroupConfig(cgroup.name, cgroup.hierarchy);
						});
						actionCell.appendChild(viewButton);

						// 修改组配置按钮
						const editButton = document.createElement('button');
						editButton.textContent = '修改组配置';
						editButton.className = 'button';
						editButton.addEventListener('click', function() {
							editGroupConfig(cgroup.name, cgroup.hierarchy);
						});
						actionCell.appendChild(editButton);

						// 创建子组按钮
						const createSubgroupButton = document.createElement('button');
						createSubgroupButton.textContent = '创建子组';
						createSubgroupButton.className = 'button';
						createSubgroupButton.addEventListener('click', function() {
							createSubgroup(cgroup.name, cgroup.hierarchy);
						});
						actionCell.appendChild(createSubgroupButton);

						// 如果不是根路径则显示删除按钮
						if (cgroupPath !== '/sys/fs/cgroup') {
							const deleteButton = document.createElement('button');
							deleteButton.textContent = '删除组';
							deleteButton.className = 'button';
							deleteButton.addEventListener('click', function() {
								deleteGroup(cgroup.name, cgroup.hierarchy);
							});
							actionCell.appendChild(deleteButton);
						}

						row.appendChild(actionCell);

						// 添加到表格中，确保子组在父组之后显示
						const tableBody = document.getElementById('cgroup-table-body');
						if (parentRow) {
							parentRow.insertAdjacentElement('afterend', row);
						} else {
							tableBody.appendChild(row);
						}
					});
				}
			})
			.catch(error => console.error('Error loading cgroups:', error));
	}

	// 删除子组行的函数，只删除子级路径
	function removeSubGroups(cgroupPath) {
		const tableBody = document.getElementById('cgroup-table-body');
		const rows = tableBody.querySelectorAll('tr[data-path]');
		rows.forEach(row => {
			const rowPath = row.getAttribute('data-path');
			// 只删除子路径，而不是父路径本身
			if (rowPath.startsWith(cgroupPath + '/') && rowPath !== cgroupPath) {
				row.remove();
			}
		});
	}

	// 删除组
	function deleteGroup(name, hierarchy) {
		if (confirm("确定要删除该组吗？")) {
			fetch('http://192.168.81.134:8081/api/user/deleteCgroup', {
					method: 'DELETE',
					headers: {
						'Content-Type': 'application/json'
					},
					body: JSON.stringify({
						name,
						hierarchy
					})
				})
				.then(response => response.json())
				.then(data => {
					if (data.status === 200) {
						const successMessage = document.createElement('div');
						successMessage.className = 'success-message';
						successMessage.textContent = data.message;
						document.body.appendChild(successMessage);
						setTimeout(() => successMessage.remove(), 3000);
						tableBody.innerHTML = ` `;
						loadCgroup('/sys/fs/cgroup'); // 重新加载组
					} else if (data.status === 409) {
						const errorMessage = document.createElement('div');
						errorMessage.className = 'error-message';
						errorMessage.textContent = data.message;
						document.body.appendChild(errorMessage);
						setTimeout(() => errorMessage.remove(), 3000);
					} else if (data.status === 400) {
						const errorMessage = document.createElement('div');
						errorMessage.className = 'error-message';
						errorMessage.textContent = data.message;
						document.body.appendChild(errorMessage);
						setTimeout(() => errorMessage.remove(), 3000);
					} else if (data.status === 500) {
						const errorMessage = document.createElement('div');
						errorMessage.className = 'error-message';
						errorMessage.textContent = data.message;
						document.body.appendChild(errorMessage);
						setTimeout(() => errorMessage.remove(), 3000);
					}
				})
				.catch(error => console.error('Error:', error));
		}
	}

	// 创建子组的浮动框弹出和数据提交
	function createSubgroup(parentName, parentHierarchy) {
		// 弹出浮动框的HTML
		const modal = document.createElement('div');
		modal.innerHTML = `
			<div class="modal">
				<h2>创建子组</h2>
				<label>名称: <input type="text" id="subgroup-name"></label><br>
				<label>描述: <input type="text" id="subgroup-description"></label><br>
				<button id="create-subgroup-button">创建</button>
				<button id="cancel-button">取消</button>
			</div>
		`;
		document.body.appendChild(modal);

		// 创建子组按钮点击事件
		document.getElementById('create-subgroup-button').addEventListener('click', function() {
			const name = document.getElementById('subgroup-name').value;
			const description = document.getElementById('subgroup-description').value;

			fetch('http://192.168.81.134:8081/api/user/createCgroup', {
					method: 'POST',
					headers: {
						'Content-Type': 'application/json'
					},
					body: JSON.stringify({
						parentName,
						parentHierarchy,
						name,
						description
					})
				})
				.then(response => response.json())
				.then(data => {
					if (data.status === 200) {
						const successMessage = document.createElement('div');
						successMessage.className = 'success-message';
						successMessage.textContent = data.message;
						document.body.appendChild(successMessage);
						setTimeout(() => successMessage.remove(), 3000);
						document.body.removeChild(modal);
						tableBody.innerHTML = ` `;
						loadCgroup('/sys/fs/cgroup'); // 重新加载组
					} else {
						const errorMessage = document.createElement('div');
						errorMessage.className = 'error-message';
						errorMessage.textContent = data.message;
						document.body.appendChild(errorMessage);
						setTimeout(() => errorMessage.remove(), 3000);
					}
				})
				.catch(error => console.error('Error:', error));
		});

		// 取消按钮点击事件
		document.getElementById('cancel-button').addEventListener('click', function() {
			document.body.removeChild(modal);
		});
	}

	// 查看组配置的浮动框弹出和数据展示
	function viewGroupConfig(name, hierarchy) {
		// 弹出浮动框的HTML
		const modal = document.createElement('div');
		modal.innerHTML = `
			<div class="modal">
				<h2>查看组配置</h2>
				<div class="tabs">
					<span data-controller-id="01" class="tab">CPU控制器</span>
					<span data-controller-id="02" class="tab">内存控制器</span>
					<span data-controller-id="03" class="tab">进程控制器</span>
					<span data-controller-id="04" class="tab">CPUSET控制器</span>
					<span data-controller-id="05" class="tab">IO控制器</span>
				</div>
				<div id="config-content"></div>
				<button id="close-button">关闭</button>
			</div>
		`;
		document.body.appendChild(modal);

		// 关闭按钮点击事件
		document.getElementById('close-button').addEventListener('click', function() {
			document.body.removeChild(modal);
		});

		// Tab点击事件
		document.querySelectorAll('.tab').forEach(tab => {
			tab.addEventListener('click', function() {
				const controllerID = this.getAttribute('data-controller-id');
				fetch(
						`http://192.168.81.134:8081/api/user/viewCgroupConfig?name=${name}&hierarchy=${hierarchy}&controllerID=${controllerID}&filter=0`
					)
					.then(response => response.json())
					.then(data => {
						if (data === null) {
							throw new Error('Request failed with status ');
						}
						if (data.status === 200) {
							const successMessage = document.createElement('div');
							successMessage.className = 'success-message';
							successMessage.textContent = data.message;
							document.body.appendChild(successMessage);
							setTimeout(() => successMessage.remove(), 3000);
							const contentDiv = document.getElementById('config-content');
							contentDiv.innerHTML = ''; // 清空内容
							Object.entries(data).forEach(([key, value]) => {
								if (key !== 'message' && key !== 'status') {
									const configRow = document.createElement('div');
									configRow.classList.add('config-row');
									configRow.textContent = `${key}: ${value}`;
									contentDiv.appendChild(configRow);
								}
							});
						} else {
							const errorMessage = document.createElement('div');
							errorMessage.className = 'error-message';
							errorMessage.textContent = data.message;
							document.body.appendChild(errorMessage);
							setTimeout(() => errorMessage.remove(), 3000);
						}
					})
					.catch(error => console.error('Error:', error));
			});
		});

		// 默认加载第一个tab的内容
		document.querySelector('.tab').click();
	}

	// 修改组配置的浮动框弹出和数据展示
	function editGroupConfig(name, hierarchy) {
		// 弹出浮动框的HTML
		const modal = document.createElement('div');
		modal.innerHTML = `
			<div class="modal">
				<h2>修改组配置</h2>
				<div class="tabs">
					<span data-controller-id="01" class="tab">CPU控制器</span>
					<span data-controller-id="02" class="tab">内存控制器</span>
					<span data-controller-id="03" class="tab">进程控制器</span>
					<span data-controller-id="04" class="tab">CPUSET控制器</span>
					<span data-controller-id="05" class="tab">IO控制器</span>
				</div>
				<div id="config-content"></div>
				<button id="close-button">关闭</button>
			</div>
		`;
		document.body.appendChild(modal);

		// 关闭按钮点击事件
		document.getElementById('close-button').addEventListener('click', function() {
			document.body.removeChild(modal);
		});

		// Tab点击事件
		document.querySelectorAll('.tab').forEach(tab => {
			tab.addEventListener('click', function() {
				const controllerID = this.getAttribute('data-controller-id');
				fetch(
						`http://192.168.81.134:8081/api/user/viewCgroupConfig?name=${name}&hierarchy=${hierarchy}&controllerID=${controllerID}&filter=1`
					)
					.then(response => response.json())
					.then(data => {
						if (data === null) {
							throw new Error('Request failed with status ');
						}

						if (data.status === 200) {
							const successMessage = document.createElement('div');
							successMessage.className = 'success-message';
							successMessage.textContent = data.message;
							document.body.appendChild(successMessage);
							setTimeout(() => successMessage.remove(), 3000);
							const contentDiv = document.getElementById('config-content');
							contentDiv.innerHTML = ''; // 清空内容
							Object.entries(data).forEach(([key, value]) => {
								if (key !== 'message' && key !== 'status') {
									const configRow = document.createElement('div');
									configRow.classList.add('config-row');
									configRow.innerHTML = `
									<span>${key}: </span>
									<input type="text" value="${value}" id="config-${key}">
									<button data-key="${key}" class="save-config-button">保存</button>
								`;
									contentDiv.appendChild(configRow);
								}
							});
						} else {
							const errorMessage = document.createElement('div');
							errorMessage.className = 'error-message';
							errorMessage.textContent = data.message;
							document.body.appendChild(errorMessage);
							setTimeout(() => errorMessage.remove(), 3000);
						}

						// 保存按钮点击事件
						document.querySelectorAll('.save-config-button').forEach(button => {
							button.addEventListener('click', function() {
								const key = this.getAttribute('data-key');
								const value = document.getElementById(
									`config-${key}`).value;

								fetch('http://192.168.81.134:8081/api/user/updateCgroupConfig', {
										method: 'PUT',
										headers: {
											'Content-Type': 'application/json'
										},
										body: JSON.stringify({
											name,
											hierarchy,
											config: {
												key,
												value
											}
										})
									})
									.then(response => response.json())
									.then(data => {
										if (data.status === 200) {
											const successMessage =
												document.createElement(
													'div');
											successMessage.className =
												'success-message';
											successMessage.textContent =
												data.message;
											document.body.appendChild(
												successMessage);
											setTimeout(() =>
												successMessage
												.remove(), 3000);
											tableBody.innerHTML = ` `;
											loadCgroup(
												'/sys/fs/cgroup'
											); // 重新加载组
										} else {
											const errorMessage =
												document.createElement(
													'div');
											errorMessage.className =
												'error-message';
											errorMessage.textContent =
												data.message;
											document.body.appendChild(
												errorMessage);
											setTimeout(() =>
												errorMessage
												.remove(), 3000);
										}
									})
									.catch(error => console.error('Error:',
										error));
							});
						});
					})
					.catch(error => console.error('Error:', error));
			});
		});

		// 默认加载第一个tab的内容
		document.querySelector('.tab').click();
	}

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
});