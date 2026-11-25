"use strict";

// Время: сервер кладёт в data-time миллисекунды UTC, показываем в поясе устройства.
// Подключается на обеих страницах (статистика и область).
const timeFormat = new Intl.DateTimeFormat("ru-RU", {
    day: "2-digit", month: "long", year: "numeric",
    hour: "2-digit", minute: "2-digit", second: "2-digit",
    timeZoneName: "short"
});

document.querySelectorAll("[data-time]").forEach(cell => {
    cell.textContent = timeFormat.format(new Date(Number(cell.dataset.time)));
});
