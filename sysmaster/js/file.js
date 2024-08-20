document.addEventListener('DOMContentLoaded', function() {
	const currentPath = '/kncsz'; // 初始化路径为 /kncsz
	const userPrefix = '/home/kncsz/SysMaster/file/user';
	let currentPage = 1;
	let itemsPerPage = 10;
	let selectedCgroup = null;
	let maxPage = 1;

	// 加载文件列表
	function loadFiles(path, page, itemsPerPage) {
		fetch(`http://192.168.81.134:8081/api/user/selectFile?path=${encodeURIComponent(path)}`)
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

				const files = data.files || [];
				const directories = files.filter(file => file.type === 'directory');
				const regularFiles = files.filter(file => file.type !== 'directory');
				const sortedFiles = directories.concat(regularFiles);

				// 清空现有列表
				const fileListTbody = document.querySelector('.file-list tbody');
				fileListTbody.innerHTML = '';

				// 全选复选框逻辑
				const selectAllCheckbox = document.getElementById('selectAllCheckbox');
				selectAllCheckbox.addEventListener('change', function() {
					const checkboxes = document.querySelectorAll(
						'.file-list tbody input[type="checkbox"]');
					checkboxes.forEach(checkbox => {
						checkbox.checked = selectAllCheckbox.checked;
					});
				});

				// 填充文件数据
				sortedFiles.slice((page - 1) * itemsPerPage, page * itemsPerPage).forEach(file => {
					const row = document.createElement('tr');

					// 选择框--》
					const checkboxCell = document.createElement('td');
					const checkbox = document.createElement('input');
					checkbox.type = 'checkbox';
					checkboxCell.appendChild(checkbox);

					// 《-- 选择框

					const nameCell = document.createElement('td');
					nameCell.textContent = file.name;

					const typeCell = document.createElement('td');
					typeCell.textContent = file.type;

					const sizeCell = document.createElement('td');
					sizeCell.textContent = file.size;

					const dateCell = document.createElement('td');
					dateCell.textContent = file.datestamp;

					const descriptionCell = document.createElement('td');
					descriptionCell.textContent = file.description;

					const actionCell = document.createElement('td');

					// Check file type to decide which buttons to show
					if (file.type === 'directory') {
						const folderIcon = document.createElement('img');
						folderIcon.src =
							'https://tse3-mm.cn.bing.net/th/id/OIP-C.zVhS14b9PeQjxFsFxm-_qgHaHa?w=2400&h=2400&rs=1&pid=ImgDetMain'; // 文件夹图标路径
						folderIcon.alt = 'Folder';
						folderIcon.className = 'folder-icon';
						folderIcon.style.width = '24px'; // 你可以根据需要调整
						folderIcon.style.height = '24px'; // 你可以根据需要调整

						// 鼠标悬停时改变样式
						folderIcon.addEventListener('mouseover', () => {
							folderIcon.style.cursor = 'pointer';
							folderIcon.style.opacity = '0.7';
						});
						folderIcon.addEventListener('mouseout', () => {
							folderIcon.style.opacity = '1.0';
						});

						// 点击事件
						folderIcon.addEventListener('click', () => {
							const currentPath = file.path + '/' + file.name
							loadFiles(currentPath.replace(userPrefix, ''), 1,
								itemsPerPage); // 加载子目录
						});

						nameCell.appendChild(folderIcon);

						const uploadBtn = document.createElement('button');
						uploadBtn.textContent = '上传文件';
						uploadBtn.className = 'button';
						uploadBtn.addEventListener('click', function() {
							const relativePath = file.path.replace(userPrefix, '');
							showUploadFileModal(relativePath);
						});

						const createDirBtn = document.createElement('button');
						createDirBtn.textContent = '创建目录';
						createDirBtn.className = 'button';
						createDirBtn.addEventListener('click', function() {
							const relativePath = file.path.replace(userPrefix, '');
							showCreateDirModal(relativePath);
						});

						actionCell.appendChild(uploadBtn);
						actionCell.appendChild(createDirBtn);
					} else {
						const createTaskBtn = document.createElement('button');
						createTaskBtn.textContent = '创建任务';
						createTaskBtn.className = 'button';
						createTaskBtn.addEventListener('click', function() {
							handleCreateTask(file);
						});

						actionCell.appendChild(createTaskBtn);
					}

					// Always add delete button
					const deleteBtn = document.createElement('button');
					deleteBtn.textContent = '删除';
					deleteBtn.className = 'button';
					deleteBtn.addEventListener('click', function() {
						const relativePath = file.path.replace(userPrefix, '');
						deleteFile(relativePath, file.type, file.name);
					});

					actionCell.appendChild(deleteBtn);

					row.appendChild(checkboxCell);
					row.appendChild(nameCell);
					row.appendChild(typeCell);
					row.appendChild(sizeCell);
					row.appendChild(dateCell);
					row.appendChild(descriptionCell);
					row.appendChild(actionCell);

					fileListTbody.appendChild(row);
				});

				// 更新分页信息
				const totalItems = sortedFiles.length;
				const totalPages = Math.ceil(totalItems / itemsPerPage);
				maxPage = totalPages;
				document.getElementById('currentPage').textContent = `${currentPage}/${totalPages}`;

				// 初次加载时计算文件摘要
				calculateSummary();

				// 更新路径显示
				updatePathDisplay(path);

				// 更新最大页数并显示
				updatePaginationButtons();
			})
			.catch(error => console.error('获取数据时发生错误:', error));
	}

	// 计算文件和目录的数量及文件总大小
	function calculateSummary() {
		const rows = document.querySelectorAll(".file-list tbody tr");
		let directoryCount = 0;
		let fileCount = 0;
		let totalSize = 0;

		rows.forEach(row => {
			const type = row.querySelector("td:nth-child(3)").innerText.trim(); // 获取类型 (目录或文件)
			const size = parseInt(row.querySelector("td:nth-child(4)").innerText.trim(), 10) ||
				0; // 获取文件大小

			if (type === "directory") {
				directoryCount++;
			} else if (type === "file") {
				fileCount++;
				totalSize += size;
			}
		});

		// 更新文件摘要
		const summary = document.querySelector(".file-summary");
		summary.innerHTML =
			`共${directoryCount}个目录，${fileCount}个文件，文件大小：<span style="color: green;">${totalSize} Byte</span>`;
	}

	// 更新路径显示
	function updatePathDisplay(path) {
		const pathDisplay = document.getElementById('pathDisplay');
		const pathParts = path.split('/').filter(Boolean); // 去除空的路径部分

		// 清空显示
		pathDisplay.innerHTML = '';

		// 构建路径导航
		let currentPath = '';
		pathParts.forEach((part, index) => {
			currentPath += `/${part}`;
			const pathItem = document.createElement('span');
			pathItem.textContent = part;
			pathItem.classList.add('path-link');
			pathItem.setAttribute('data-path', currentPath);

			// 点击事件加载对应目录
			pathItem.addEventListener('click', function() {
				const newPath = this.getAttribute('data-path');
				loadFiles(newPath, 1, itemsPerPage);
			});

			// 鼠标悬停样式
			pathItem.addEventListener('mouseover', function() {
				pathItem.style.textDecoration = 'underline';
				pathItem.style.cursor = 'pointer';
			});
			pathItem.addEventListener('mouseout', function() {
				pathItem.style.textDecoration = 'none';
			});

			pathDisplay.appendChild(pathItem);

			// 添加分隔符
			if (index < pathParts.length - 1) {
				pathDisplay.appendChild(document.createTextNode(' > '));
			}
		});
	}

	// 处理创建任务操作
	function handleCreateTask(file) {
		const invalidExtensions = ['.jar', '.py', '.sh', '.class', '.exe'];

		// 检查文件是否是可执行的
		const isExecutable = invalidExtensions.some(ext => file.name.endsWith(ext)) || !file.name.includes('.');
		if (!isExecutable) {
			const errorMessage = document.createElement('div');
			errorMessage.className = 'error-message';
			errorMessage.textContent = '文件不可执行';
			document.body.appendChild(errorMessage);
			setTimeout(() => errorMessage.remove(), 3000);
			return;
		}

		const modal = document.createElement('div');
		modal.className = 'modal';
		modal.innerHTML = `
			<div class="modal-header">创建任务</div>
			<div class="modal-body">
				<label for="taskName">任务名称:</label>
				<input type="text" id="taskName">
				<br><br>
				<label for="command">命令:</label>
				<input type="text" id="command">
				<br><br>
				<label for="description">描述:</label>
				<input type="text" id="description">
				<br><br>
				<label for="cgroup">选择组:</label>
				<div id="cgroupList"></div>
			</div>
			<div class="modal-footer">
				<button id="closeModal">关闭</button>
				<button id="createTask">创建</button>
			</div>
		`;
		document.body.appendChild(modal);
		modal.style.display = 'block';

		// 获取 cgroup 组列表
		fetch('http://192.168.81.134:8081/api/user/selectCgroup')
			.then(response => response.json())
			.then(data => {
				if(data === null){
					throw new Error('Request failed with status ');
				}
				
				if(data.status === 200){
					if (data.cgroups) {
						data.cgroups.forEach(cgroup => {
							const cgroupItem = document.createElement('div');
							cgroupItem.textContent = `${cgroup.name} (${cgroup.cgroupPath})`;
							const addButton = document.createElement('button');
							addButton.textContent = '+';
							addButton.className = 'plus-button';  // 添加样式类
							addButton.addEventListener('click', () => {
								document.querySelectorAll('.plus-button').forEach(btn => btn.classList.remove('selected'));
								addButton.classList.add('selected');
								selectedCgroup = cgroup;
							});
							cgroupItem.appendChild(addButton);
							cgroupList.appendChild(cgroupItem);
						});
					}
				} else if(data.status === 400){
					const errorMessage = document.createElement('div');
					errorMessage.className = 'error-message';
					errorMessage.textContent = '查询错误';
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

		document.getElementById('closeModal').addEventListener('click', function() {
			modal.remove();
		});

		document.getElementById('createTask').addEventListener('click', function() {
			const taskName = document.getElementById('taskName').value;
			const command = document.getElementById('command').value;
			const description = document.getElementById('description').value;

			const relativePath = file.path.replace(userPrefix, '');
			console.log(command, taskName, selectedCgroup.cgroupPath, selectedCgroup.cgroupConfigDir, description, selectedCgroup.name, file.name, file.path);
			const payload = {
				commands: [command],
				taskName: taskName,
				cgroupPath: selectedCgroup.cgroupPath,
				cgroupConfigDir: selectedCgroup.cgroupConfigDir,
				description: description,
				cgroupName: selectedCgroup.name,
				fileName: file.name,
				filePath: file.path
			};

			fetch('http://192.168.81.134:8081/api/user/createTask', {
				method: 'POST',
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
				
				if (data.status === 200) {
					const successMessage = document.createElement('div');
					successMessage.className = 'success-message';
					successMessage.textContent = '任务创建成功';
					document.body.appendChild(successMessage);
					setTimeout(() => successMessage.remove(), 3000);
					modal.remove();
				} else if(data.status == 409){
					const errorMessage = document.createElement('div');
					errorMessage.className = 'error-message';
					errorMessage.textContent = '任务名冲突';
					document.body.appendChild(errorMessage);
					setTimeout(() => errorMessage.remove(), 3000);
				} else if(data.status == 400){
					const errorMessage = document.createElement('div');
					errorMessage.className = 'error-message';
					errorMessage.textContent = '请求错误';
					document.body.appendChild(errorMessage);
					setTimeout(() => errorMessage.remove(), 3000);
				} else if(data.status == 500){
					const errorMessage = document.createElement('div');
					errorMessage.className = 'error-message';
					errorMessage.textContent = '服务器错误';
					document.body.appendChild(errorMessage);
					setTimeout(() => errorMessage.remove(), 3000);
				}
			})
			.catch(error => console.error('创建任务时发生错误:', error));
		});
	}

	// 文件上传操作
	function uploadFile(path) {
		showUploadFileModal(path);
	}

	// 创建目录操作
	function createDirectory(path) {
		showCreateDirModal(path);
	}

	// 删除文件/目录操作
	function deleteFile(path, type, name) {
		fetch('http://192.168.81.134:8081/api/user/deleteFile', {
			method: 'DELETE',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({
				path: path,
				type: type,
				name: name
			})
		})
		.then(response => response.json())
		.then(data => {
			if(data === null){
				throw new Error('Request failed with status ');
			}
			
			if (data.status === 200) {
				const successMessage = document.createElement('div');
				successMessage.className = 'success-message';
				successMessage.textContent = '删除成功';
				document.body.appendChild(successMessage);
				setTimeout(() => successMessage.remove(), 3000);
				loadFiles(currentPath, currentPage, itemsPerPage);
			} else if(data.status === 404){
				const errorMessage = document.createElement('div');
				errorMessage.className = 'error-message';
				errorMessage.textContent = '文件不存在';
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
		.catch(error => console.error('删除文件时发生错误:', error));
	}

	// 处理上传文件按钮
	const uploadFileButton = document.getElementById('uploadFileBtn');
	uploadFileButton.addEventListener('click',
		function() {
			showUploadFileModal(currentPath);
		});

	// 处理创建目录按钮
	const createDirButton = document.getElementById('createDirBtn');
	createDirButton.addEventListener('click',
		function() {
			showCreateDirModal(currentPath);
		});

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

	// 处理分页按钮
	document.getElementById('prevPageBtn').addEventListener('click', function() {
		if (currentPage > 1) {
			currentPage--;
			loadFiles(currentPath, currentPage, itemsPerPage);
		}
	});

	document.getElementById('nextPageBtn').addEventListener('click', function() {
		if (currentPage < maxPage) { // 只有当前页小于最大页时才允许翻页
			currentPage++;
			loadFiles(currentPath, currentPage, itemsPerPage);
		}
	});

	document.getElementById('itemsPerPage').addEventListener('change', function(event) {
		itemsPerPage = parseInt(event.target.value, 10);
		currentPage = 1; // 重置到第一页
		loadFiles(currentPath, currentPage, itemsPerPage);
	});

	// 更新按钮状态
	function updatePaginationButtons() {

		if (currentPage === 1) {
			document.getElementById('prevPageBtn').disabled = true;
		} else {
			document.getElementById('prevPageBtn').disabled = false;
		}

		if (currentPage === maxPage) {
			document.getElementById('nextPageBtn').disabled = true;
		} else {
			document.getElementById('nextPageBtn').disabled = false;
		}
	}


	// 显示上传文件浮动框
	function showUploadFileModal(currentPath) {
		const modal = document.createElement('div');
		modal.className = 'modal';
		modal.innerHTML = `
            <div class="modal-header">上传文件</div>
            <div class="modal-body">
                <label for="description">描述:</label>
                <input type="text" id="description">
                <br><br>
                <label for="file">选择文件:</label>
                <input type="file" id="file">
            </div>
            <div class="modal-footer">
                <button id="closeModal">关闭</button>
                <button id="uploadFile">上传</button>
            </div>
        `;
		document.body.appendChild(modal);
		modal.style.display = 'block';

		document.getElementById('closeModal').addEventListener('click', function() {
			modal.remove();
		});

		document.getElementById('uploadFile').addEventListener('click', function() {
			const description = document.getElementById('description').value;
			const fileInput = document.getElementById('file');
			const file = fileInput.files[0];
			if (file) {
				const formData = new FormData();
				formData.append('file', file);
				formData.append('path', currentPath);
				formData.append('description', description);

				fetch('http://192.168.81.134:8081/api/user/uploadFile', {
					method: 'POST',
					body: formData
				})
				.then(response => response.json())
				.then(data => {
					if(data === null){
						throw new Error('Request failed with status ');
					}
					
					if (data.status === 200) {
						const successMessage = document.createElement('div');
						successMessage.className = 'success-message';
						successMessage.textContent = '上传成功';
						document.body.appendChild(successMessage);
						setTimeout(() => successMessage.remove(), 3000);
						modal.remove();
						loadFiles(currentPath, currentPage, itemsPerPage);
					} else if(data.status === 413){
						const errorMessage = document.createElement('div');
						errorMessage.className = 'error-message';
						errorMessage.textContent = '文件过大';
						document.body.appendChild(errorMessage);
						setTimeout(() => errorMessage.remove(), 3000);
					} else if(data.status === 400){
						const errorMessage = document.createElement('div');
						errorMessage.className = 'error-message';
						errorMessage.textContent = '文件不合法';
						document.body.appendChild(errorMessage);
						setTimeout(() => errorMessage.remove(), 3000);
					} else if(data.status === 500){
						const errorMessage = document.createElement('div');
						errorMessage.className = 'error-message';
						errorMessage.textContent = '服务器错误';
						document.body.appendChild(errorMessage);
						setTimeout(() => errorMessage.remove(), 3000);
					} else if(data.status === 10000){
						const errorMessage = document.createElement('div');
						errorMessage.className = 'error-message';
						errorMessage.textContent = '文件存在风险，上传被终止！';
						document.body.appendChild(errorMessage);
						setTimeout(() => errorMessage.remove(), 3000);
					}
				})
				.catch(error => console.error('上传文件时发生错误:', error));
			} else {
				alert('请选择文件');
			}
		});
	}

	// 显示创建目录浮动框
	function showCreateDirModal(currentPath) {
		const modal = document.createElement('div');
		modal.className = 'modal';
		modal.innerHTML = `
            <div class="modal-header">创建目录</div>
            <div class="modal-body">
                <label for="dirName">目录名称:</label>
                <input type="text" id="dirName">
                <br><br>
                <label for="description">描述:</label>
                <input type="text" id="description">
            </div>
            <div class="modal-footer">
                <button id="closeModal">关闭</button>
                <button id="createDir">创建</button>
            </div>
        `;
		document.body.appendChild(modal);
		modal.style.display = 'block';

		document.getElementById('closeModal').addEventListener('click', function() {
			modal.remove();
		});

		document.getElementById('createDir').addEventListener('click', function() {
			const dirName = document.getElementById('dirName').value;
			const description = document.getElementById('description').value;
			if (dirName) {
				const payload = {
					path: currentPath,
					name: dirName,
					description: description
				};

				fetch('http://192.168.81.134:8081/api/user/createDirectory', {
					method: 'POST',
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
					
					if (data.status === 200) {
						// 优雅地提示
						const successMessage = document.createElement('div');
						successMessage.className = 'success-message';
						successMessage.textContent = '目录创建成功';
						document.body.appendChild(successMessage);
						setTimeout(() => successMessage.remove(), 3000); 
						modal.remove();
						loadFiles(currentPath, currentPage, itemsPerPage);
					} else if(data.status === 409){
						const errorMessage = document.createElement('div');
						errorMessage.className = 'error-message';
						errorMessage.textContent = '目录名不能重复';
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
				.catch(error => console.error('创建目录时发生错误:', error));
			} else {
				const errorMessage = document.createElement('div');
				errorMessage.className = 'error-message';
				errorMessage.textContent = '目录名称不能为空';
				document.body.appendChild(errorMessage);
				setTimeout(() => errorMessage.remove(), 3000);
				modal.remove();
			}
		});
	}

	// 初始加载
	loadFiles(currentPath, currentPage, itemsPerPage);
});