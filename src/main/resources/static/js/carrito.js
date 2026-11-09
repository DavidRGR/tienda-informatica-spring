(function () {
	const panelEl = document.getElementById('panelCarrito');
	if (!panelEl) return;

	const contenido = document.getElementById('panelCarritoContenido');
	const panel = bootstrap.Offcanvas.getOrCreateInstance(panelEl);
	let contenidoFresco = false; // Evita recargar si acabamos de pintar la respuesta de una acción

	function actualizarContador(unidades) {
		const contador = document.getElementById('contadorCarrito');
		if (!contador) return;
		contador.textContent = unidades;
		contador.classList.toggle('d-done', unidades === 0);
	}

	function pintar(html){
		contenido.innerHTML = html;
		const marca = contenido.querySelector('[data-unidades]');
		actualizarContador(marca ? Number(marca.dataset.unidades) : 0);
	}

	function mostrarError(texto){
		contenido.insertAdjacentHTML('afterbegin',
			'<div class="alert alert-danger m-3 py-2 small">' + texto + '</div>');
	}

	async function cargar() {
		contenido.innerHTML = '<div class="p-4 text-center text-muted">Cargando…</div>';
		try {
			const r = await fetch('/carrito/panel', { headers: { 'X-Requested-With': 'fetch' } });
			if (!r.ok) throw new Error();
			pintar(await r.text());
		} catch (e) {
			contenido.innerHTML = '';
			mostrarError("No se pudo cargar el carrito");
		}
	}

	// Al abrir el panel con el icono del navbar se carga el contenido
	panelEl.addEventListener('show.bs.offcanvas', () => {
			if (!contenidoFresco) cargar();
			contenidoFresco = false;
	});

	// Los formularios marcados con data-carrito se envían en segundo plano
	document.addEventListener('submit', async (evento) => {
		const form = evento.target.closest('form[data-carrito]');
		if (!form) return;
		evento.preventDefault();

		const boton = form.querySelector('button[type="submit"]');
		if (boton) boton.disabled = true;

		try {
			const r = await fetch(form.action, {
				method: 'POST',
				body: new URLSearchParams(new FormData(form)),
				headers: { 'X-Requested-With': 'fetch' }
			});
			if (r.redirected) { // sesión caducada: nos mandan al login
				window.location.href = r.url;
				return;
			}
			if (!r.ok) throw new Error();
			pintar(await r.text());
			if (!panelEl.classList.contains('show')) {
				contenidoFresco = true;
				panel.show();
			}
		} catch (e) {
			if (!panelEl.classList.contains('show')) { contenidoFresco = true; panel.show(); }
			mostrarError('No se pudo completar la acción. Inténtalo de nuevo.');
		} finally {
			if (boton) boton.disabled = false;
		}
	});



})();