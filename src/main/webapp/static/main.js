"use strict";

// Настройки варианта
const X_VALUES = [-4, -3, -2, -1, 0, 1, 2, 3, 4];
const R_VALUES = [1, 2, 3, 4, 5];
const Y_MIN = -3;
const Y_MAX = 5;
const CLICK_MIN = -5;   // клик по графику: X и Y в интервале (-5, 5)
const CLICK_MAX = 5;

// Элементы страницы
const form = document.getElementById("point-form");
const checkUrl = form.getAttribute("action");   // "/lab4/check": путь приложения уже добавил Thymeleaf
const xGroup = document.getElementById("x-group");
const xInput = document.getElementById("x-input");
const yInput = document.getElementById("y-input");
const rSelect = document.getElementById("r-select");
const xError = document.getElementById("x-error");
const yError = document.getElementById("y-error");
const rError = document.getElementById("r-error");
const plotError = document.getElementById("plot-error");
const canvas = document.getElementById("plot");
const ctx = canvas.getContext("2d");

// Константы холста
const W = canvas.width;
const H = canvas.height;
const CX = W / 2;
const CY = H / 2;
const UNIT = W / 2 / 6;

// Результаты прошлых проверок: шаблон вывел их в таблицу из сессии, берём из data-атрибутов
const results = Array.from(document.querySelectorAll("#results-body tr")).map(tr => ({
    x: Number(tr.dataset.x),
    y: Number(tr.dataset.y),
    r: Number(tr.dataset.r),
    hit: tr.dataset.hit === "true"
}));

function getSelectedR() {
    return rSelect.value === "" ? null : Number(rSelect.value);
}

let selectedR = getSelectedR();

// Валидация Y
function parseNumber(raw, fieldName, min, max) {
    const s = raw.trim().replace(",", ".");
    if (s === "") return {error: `Введите значение ${fieldName}`};
    if (!/^[-+]?\d{1,2}(\.\d{1,10})?$/.test(s)) {
        return {error: `${fieldName} должен быть числом (не более 10 знаков после точки)`};
    }
    const n = Number(s);
    if (!(n > min && n < max)) {
        return {error: `${fieldName} должен быть в интервале (${min}, ${max})`};
    }
    return {value: n, text: s};
}

// Запрет ввода лишних символов в Y
yInput.addEventListener("input", () => {
    yInput.value = yInput.value.replace(/[^0-9.,+-]/g, "");
    yError.textContent = "";
    yInput.classList.remove("invalid");
});

// Кнопки X: один обработчик на всю группу (делегирование событий).
// closest находит кнопку, даже если клик пришёлся во вложенный элемент.
xGroup.addEventListener("click", event => {
    const btn = event.target.closest(".x-btn");
    if (!btn) return;
    xGroup.querySelectorAll(".x-btn").forEach(b => b.classList.remove("selected"));
    btn.classList.add("selected");
    xInput.value = btn.dataset.value;   // это значение и уйдёт на сервер как x
    xError.textContent = "";
    xGroup.classList.remove("invalid");
});

rSelect.addEventListener("change", () => {
    selectedR = getSelectedR();
    rError.textContent = "";
    rSelect.classList.remove("invalid");
    plotError.textContent = "";
    drawPlot();
});

// Отправка формы: если есть ошибка, отменяем отправку; иначе браузер сам отправит GET
form.addEventListener("submit", event => {
    let ok = true;

    if (xInput.value === "" || !X_VALUES.includes(Number(xInput.value))) {
        xError.textContent = "Выберите значение X";
        xGroup.classList.add("invalid");
        ok = false;
    }

    const yRes = parseNumber(yInput.value, "Y", Y_MIN, Y_MAX);
    if (yRes.error) {
        yError.textContent = yRes.error;
        yInput.classList.add("invalid");
        ok = false;
    } else {
        yInput.value = yRes.text;   // уйдёт на сервер с точкой вместо запятой
    }

    if (!R_VALUES.includes(selectedR)) {
        rError.textContent = "Выберите значение R";
        rSelect.classList.add("invalid");
        ok = false;
    }

    if (!ok) event.preventDefault();
});

// Клик по графику
canvas.addEventListener("click", event => {
    if (selectedR === null) {
        plotError.textContent = "Невозможно определить координаты: сначала выберите R";
        return;
    }

    // Пиксели внутри canvas; домножаем, если CSS ужал canvas (max-width: 100%)
    const rect = canvas.getBoundingClientRect();
    const pxX = (event.clientX - rect.left) * (W / rect.width);
    const pxY = (event.clientY - rect.top) * (H / rect.height);

    const x = round3((pxX - CX) / UNIT);
    const y = round3((CY - pxY) / UNIT);   // на canvas y растёт вниз

    if (!(x > CLICK_MIN && x < CLICK_MAX && y > CLICK_MIN && y < CLICK_MAX)) {
        plotError.textContent = `Точка вне интервала: X и Y должны быть в (${CLICK_MIN}, ${CLICK_MAX})`;
        return;
    }

    // Тот же GET, что и у формы; source=click — X и Y проверяются только по интервалу
    const params = new URLSearchParams({x: x, y: y, r: selectedR, source: "click"});
    window.location.href = `${checkUrl}?${params}`;
});

function round3(value) {
    const n = Math.round(value * 1000) / 1000;
    return Object.is(n, -0) ? 0 : n;   // чтобы не отправить "-0"
}

// Canvas
function px(x) {
    return CX + x * UNIT;
}

function py(y) {
    return CY - y * UNIT;
}

function drawArea(r) {
    ctx.fillStyle = "rgba(51, 153, 255, 0.55)";

    // Треугольник (I четверть): (0,0), (R,0), (0,R)
    ctx.beginPath();
    ctx.moveTo(px(0), py(0));
    ctx.lineTo(px(r), py(0));
    ctx.lineTo(px(0), py(r));
    ctx.closePath();
    ctx.fill();

    // Прямоугольник (II четверть): x от -R до 0, y от 0 до R
    ctx.fillRect(px(-r), py(r), r * UNIT, r * UNIT);

    // Четверть круга радиуса R/2 (III четверть): углы от π/2 (вниз) до π (влево)
    ctx.beginPath();
    ctx.moveTo(px(0), py(0));
    ctx.arc(px(0), py(0), (r / 2) * UNIT, Math.PI / 2, Math.PI);
    ctx.closePath();
    ctx.fill();
}

function drawAxes() {
    ctx.strokeStyle = "#222";
    ctx.fillStyle = "#222";
    ctx.lineWidth = 1;
    ctx.font = "12px Arial, sans-serif";

    ctx.beginPath();
    ctx.moveTo(0, CY);
    ctx.lineTo(W, CY);
    ctx.moveTo(CX, H);
    ctx.lineTo(CX, 0);
    ctx.stroke();

    // Стрелки
    ctx.beginPath();
    ctx.moveTo(W, CY);
    ctx.lineTo(W - 10, CY - 5);
    ctx.lineTo(W - 10, CY + 5);
    ctx.moveTo(CX, 0);
    ctx.lineTo(CX - 5, 10);
    ctx.lineTo(CX + 5, 10);
    ctx.fill();
    ctx.fillText("x", W - 12, CY - 10);
    ctx.fillText("y", CX + 10, 12);

    // Деления
    for (let i = -5; i <= 5; i++) {
        if (i === 0) continue;
        ctx.beginPath();
        ctx.moveTo(px(i), CY - 4);
        ctx.lineTo(px(i), CY + 4);
        ctx.moveTo(CX - 4, py(i));
        ctx.lineTo(CX + 4, py(i));
        ctx.stroke();
        ctx.fillText(String(i), px(i) - 5, CY + 16);
        ctx.fillText(String(i), CX + 8, py(i) + 4);
    }
}

function drawPlot() {
    ctx.clearRect(0, 0, W, H);

    if (selectedR !== null) {
        drawArea(selectedR);
    }
    drawAxes();

    if (selectedR === null) {
        ctx.fillStyle = "#6b6b6e";
        ctx.font = "14px Arial, sans-serif";
        ctx.fillText("Выберите R,", 8, 20);
        ctx.fillText("чтобы увидеть область", 8, 40);
    }

    // Точки с другим R: бледные и маленькие
    ctx.globalAlpha = 0.3;
    for (const e of results) {
        if (e.r === selectedR) continue;
        ctx.beginPath();
        ctx.arc(px(e.x), py(e.y), 3, 0, Math.PI * 2);
        ctx.fillStyle = e.hit ? "#1e8449" : "#c0392b";
        ctx.fill();
    }
    ctx.globalAlpha = 1;

    // Точки с текущим R: яркие, крупные, с белой обводкой
    for (const e of results) {
        if (e.r !== selectedR) continue;
        ctx.beginPath();
        ctx.arc(px(e.x), py(e.y), 5, 0, Math.PI * 2);
        ctx.fillStyle = e.hit ? "#1e8449" : "#c0392b";
        ctx.fill();
        ctx.lineWidth = 1.5;
        ctx.strokeStyle = "#ffffff";
        ctx.stroke();
    }
    ctx.lineWidth = 1;
}

drawPlot();
