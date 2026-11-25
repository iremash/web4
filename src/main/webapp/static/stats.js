"use strict";

// Модальное окно подтверждения очистки (вместо встроенного confirm()).
// Окно — обычный div; показываем и прячем его классом open (см. .modal-overlay в style.css).
const clearBtn = document.getElementById("clear-btn");
const modal = document.getElementById("confirm-modal");
const yesBtn = document.getElementById("confirm-yes");
const noBtn = document.getElementById("confirm-no");

function openModal() {
    modal.classList.add("open");
    noBtn.focus();   // фокус на безопасной кнопке: случайный Enter не сотрёт данные
}

function closeModal() {
    modal.classList.remove("open");
    clearBtn.focus();   // вернуть фокус туда, откуда открыли
}

clearBtn.addEventListener("click", openModal);
noBtn.addEventListener("click", closeModal);

// Клик по затемнённому фону (но не по самому окну) — закрыть
modal.addEventListener("click", event => {
    if (event.target === modal) closeModal();
});

// Escape — закрыть
document.addEventListener("keydown", event => {
    if (event.key === "Escape" && modal.classList.contains("open")) closeModal();
});

// «Очистить» — это submit формы: браузер сам отправит POST /clear.
// Блокируем кнопку, чтобы двойной клик не отправил запрос дважды.
yesBtn.form.addEventListener("submit", () => {
    yesBtn.disabled = true;
});
