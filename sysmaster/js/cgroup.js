document.addEventListener('DOMContentLoaded', function() {
	const addCgroupButton = document.getElementById('add-cgroup');
	const cgroupModal = document.getElementById('cgroup-modal');
	const closeModalButton = document.getElementById('close-cgroup-modal');
	const closeFormButton = document.getElementById('close-form-cgroup');
	const saveAndAddButton = document.getElementById('save-and-add-cgroup');
	const cgroupTableBody = document.getElementById('cgroup-table-body');
	const cgroupConfigModal = document.getElementById('cgroup-config-modal');
	const closeConfigModalButton = document.getElementById('close-config-modal');
	const cgroupConfigDetails = document.getElementById('cgroup-config-details');
	const closeConfigButton = document.getElementById('close-config');
	let selectedCgroup = null;

	// Fetch and display cgroups
	fetch('http://192.168.81.133:8081/api/user/selectCgroup')
		.then(response => response.json())
		.then(data => {
			if (data.cgroup) {
				data.cgroup.forEach(cgroup => {
					const row = document.createElement('tr');
					row.innerHTML = `
                        <td><input type="checkbox"></td>
                        <td>${cgroup.name}</td>
                        <td>${cgroup.hierarchy}</td>
                        <td>${cgroup.datestamp}</td>
                        <td>${cgroup.description}</td>
                        <td>
                            <button class="view-config" data-name="${cgroup.name}" data-hierarchy="${cgroup.hierarchy}" data-controller-id="01">查看组配置</button>
                            <button class="edit-config" data-name="${cgroup.name}" data-hierarchy="${cgroup.hierarchy}" data-controller-id="01">修改组配置</button>
                            <button class="delete" data-name="${cgroup.name}" data-hierarchy="${cgroup.hierarchy}">删除组</button>
                            <button class="create-subgroup" data-name="${cgroup.name}" data-hierarchy="${cgroup.hierarchy}">创建子组</button>
                        </td>
                    `;
					cgroupTableBody.appendChild(row);
				});
			}
		})
		.catch(error => console.error('Error fetching cgroups:', error));

	// Open modal to add cgroup
	addCgroupButton.addEventListener('click', function() {
		cgroupModal.style.display = 'block';
	});

	// Handle "创建子组" button click to open modal
	cgroupTableBody.addEventListener('click', function(event) {
		if (event.target.classList.contains('create-subgroup')) {
			// 获取当前行的组名称和层级
			selectedCgroup = {
				name: event.target.getAttribute('data-name'),
				hierarchy: event.target.getAttribute('data-hierarchy')
			};
			// 打开模态框
			cgroupModal.style.display = 'block';
		}
	});

	// Close modals
	closeModalButton.addEventListener('click', function() {
		cgroupModal.style.display = 'none';
	});

	closeFormButton.addEventListener('click', function() {
		cgroupModal.style.display = 'none';
	});

	closeConfigModalButton.addEventListener('click', function() {
		cgroupConfigModal.style.display = 'none';
	});

	closeConfigButton.addEventListener('click', function() {
		cgroupConfigModal.style.display = 'none';
	});

	// Close modal when clicking outside of it
	window.addEventListener('click', function(event) {
		if (event.target == cgroupModal) {
			cgroupModal.style.display = 'none';
		}
		if (event.target == cgroupConfigModal) {
			cgroupConfigModal.style.display = 'none';
		}
	});

	// Handle save and add new cgroup (for subgroups)
	saveAndAddButton.addEventListener('click', function() {
		const cgroupData = {
			name: document.getElementById('cgroup-name').value,
			parentName: selectedCgroup.name,
			parentHierarchy: selectedCgroup.hierarchy
		};

		fetch('http://192.168.81.133:8081/api/user/createCgroup', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'
				},
				body: JSON.stringify(cgroupData)
			})
			.then(response => response.json())
			.then(data => {
				if (data.success) {
					alert('子组创建成功');
					// 可选：在此处清除表单字段
					cgroupModal.style.display = 'none';
				} else {
					alert('子组创建失败');
				}
			})
			.catch(error => console.error('Error creating cgroup:', error));
	});

	// Handle delete cgroup
	cgroupTableBody.addEventListener('click', function(event) {
		if (event.target.classList.contains('delete')) {
			const name = event.target.getAttribute('data-name');
			const hierarchy = event.target.getAttribute('data-hierarchy');

			fetch(`http://192.168.81.133:8081/api/user/deleteCgroup?name=${name}&hierarchy=${hierarchy}`, {
					method: 'DELETE',
					headers: {
						'Content-Type': 'application/json'
					}
				})
				.then(response => response.json())
				.then(data => {
					if (data.success) {
						alert('组删除成功');
						// Remove the cgroup row from the table
						event.target.closest('tr').remove();
					} else {
						alert('组删除失败');
					}
				})
				.catch(error => console.error('Error deleting cgroup:', error));
		}
	});

	// Handle view and edit cgroup config
	cgroupTableBody.addEventListener('click', function(event) {
		if (event.target.classList.contains('view-config') || event.target.classList.contains(
				'edit-config')) {
			const name = event.target.getAttribute('data-name');
			const hierarchy = event.target.getAttribute('data-hierarchy');
			const controllerId = event.target.getAttribute('data-controller-id');
			const isEditMode = event.target.classList.contains('edit-config');

			fetch(
					`http://192.168.81.133:8081/api/user/viewCgroupConfig?name=${name}&hierarchy=${hierarchy}&controllerID=${controllerId}`
					)
				.then(response => response.json())
				.then(data => {
					cgroupConfigDetails.innerHTML = ''; // Clear previous config
					if (data.status === 200) {
						for (const [key, value] of Object.entries(data)) {
							if (key !== 'status' && key !==
								'message') { // Skip status and message keys
								const configDiv = document.createElement('div');
								if (isEditMode) {
									configDiv.innerHTML = `
	                                    <span>${key}:</span>
	                                    <input type="text" value="${value}" data-key="${key}">
	                                    <button class="apply-config">应用</button>
	                                `;
								} else {
									configDiv.innerHTML = `
	                                    <span>${key}:</span>
	                                    <span>${value}</span>
	                                `;
								}
								cgroupConfigDetails.appendChild(configDiv);
							}
						}

						if (isEditMode) {
							// Attach click event to apply buttons
							document.querySelectorAll('.apply-config').forEach(button => {
								button.addEventListener('click', function() {
									const key = this.previousElementSibling
										.getAttribute('data-key');
									const value = this.previousElementSibling.value;

									const payload = {
										name: name,
										hierarchy: hierarchy,
										config: {
											key: key,
											value: value
										}
									};

									fetch('http://192.168.81.133:8081/api/user/updateCgroupConfig', {
											method: 'POST',
											headers: {
												'Content-Type': 'application/json'
											},
											body: JSON.stringify(payload)
										})
										.then(response => response.json())
										.then(data => {
											if (data.success) {
												alert('配置更新成功');
											} else {
												alert('配置更新失败');
											}
										})
										.catch(error => console.error(
											'Error updating config:', error));
								});
							});
						}

						cgroupConfigModal.style.display = 'block';
					} else {
						alert(`Error: ${data.message}`);
					}
				})
				.catch(error => console.error('Error fetching cgroup config:', error));
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
});