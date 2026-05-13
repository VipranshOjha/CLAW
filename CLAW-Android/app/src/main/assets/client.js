/**
 * CLAW Lite - Gamepad UI Logic
 * Handles touch inputs and translates them to an Xbox One HID byte array
 */

// Controller State Structure
const state = {
    buttons0: 0,
    buttons1: 0,
    dpad: 8,
    lx: 0,
    ly: 0,
    rx: 0,
    ry: 0
};

// D-pad state tracking
const dpadState = { up: false, down: false, left: false, right: false };

function updateDPad() {
    if (dpadState.up && dpadState.right) state.dpad = 1;
    else if (dpadState.down && dpadState.right) state.dpad = 3;
    else if (dpadState.down && dpadState.left) state.dpad = 5;
    else if (dpadState.up && dpadState.left) state.dpad = 7;
    else if (dpadState.up) state.dpad = 0;
    else if (dpadState.right) state.dpad = 2;
    else if (dpadState.down) state.dpad = 4;
    else if (dpadState.left) state.dpad = 6;
    else state.dpad = 8;
}

function sendState() {
    if (window.ClawGamepad && typeof window.ClawGamepad.sendControllerState === 'function') {
        window.ClawGamepad.sendControllerState(
            state.buttons0,
            state.buttons1,
            state.dpad,
            state.lx,
            state.ly,
            state.rx,
            state.ry
        );
    }
}

// Button Binder
function bindButton(id, byteIndex, mask) {
    const el = document.getElementById(id);
    if (!el) return;
    
    const press = (e) => {
        e.preventDefault();
        el.classList.add('active');
        if (byteIndex === 0) state.buttons0 |= mask;
        else state.buttons1 |= mask;
        sendState();
    };
    
    const release = (e) => {
        e.preventDefault();
        el.classList.remove('active');
        if (byteIndex === 0) state.buttons0 &= ~mask;
        else state.buttons1 &= ~mask;
        sendState();
    };

    el.addEventListener('touchstart', press, { passive: false });
    el.addEventListener('touchend', release, { passive: false });
    el.addEventListener('touchcancel', release, { passive: false });
}

// D-Pad Binder
function bindDPad(id, dir) {
    const el = document.getElementById(id);
    if (!el) return;

    const press = (e) => {
        e.preventDefault();
        el.classList.add('active');
        dpadState[dir] = true;
        updateDPad();
        sendState();
    };

    const release = (e) => {
        e.preventDefault();
        el.classList.remove('active');
        dpadState[dir] = false;
        updateDPad();
        sendState();
    };

    el.addEventListener('touchstart', press, { passive: false });
    el.addEventListener('touchend', release, { passive: false });
    el.addEventListener('touchcancel', release, { passive: false });
}

// Bind simple buttons (Byte 0)
// A=1, B=2, X=4, Y=8, LB=16, RB=32, Back=64, Start=128
bindButton('btn-a', 0, 1);
bindButton('btn-b', 0, 2);
bindButton('btn-x', 0, 4);
bindButton('btn-y', 0, 8);
bindButton('btn-lb', 0, 16);
bindButton('btn-rb', 0, 32);
bindButton('btn-back', 0, 64);
bindButton('btn-start', 0, 128);

// Bind extra buttons (Byte 1)
// LT=4, RT=8
bindButton('btn-lt', 1, 4);
bindButton('btn-rt', 1, 8);

// Bind D-Pad (Byte 2)
bindDPad('dpad-up', 'up');
bindDPad('dpad-down', 'down');
bindDPad('dpad-left', 'left');
bindDPad('dpad-right', 'right');

// Joystick logic (Bytes 3-6)
function bindJoystick(containerId, isLeft) {
    const container = document.getElementById(containerId);
    if (!container) return;
    const knob = container.querySelector('.stick-knob');
    
    let isDragging = false;
    let touchId = null;
    
    let isClicked = false;
    const clickMask = isLeft ? 1 : 2; // LS Click = 1, RS Click = 2 in byte 1

    const updatePosition = (x, y) => {
        const maxRadius = container.offsetWidth / 2;
        const center = { x: maxRadius, y: maxRadius };
        
        let dx = x - center.x;
        let dy = y - center.y;
        
        const distance = Math.sqrt(dx*dx + dy*dy);
        if (distance > maxRadius) {
            dx = (dx / distance) * maxRadius;
            dy = (dy / distance) * maxRadius;
        }

        // Update UI
        knob.style.transform = `translate(calc(-50% + ${dx}px), calc(-50% + ${dy}px))`;

        // Map to -127 to 127
        let outX = Math.round((dx / maxRadius) * 127);
        let outY = Math.round((dy / maxRadius) * 127);
        
        // Ensure values stay within boundary
        outX = Math.max(-127, Math.min(127, outX));
        outY = Math.max(-127, Math.min(127, outY));

        if (isLeft) {
            state.lx = outX;
            state.ly = outY;
        } else {
            state.rx = outX;
            state.ry = outY;
        }
        sendState();
    };

    container.addEventListener('touchstart', (e) => {
        e.preventDefault();
        if (isDragging) return;
        
        const touch = e.changedTouches[0];
        touchId = touch.identifier;
        isDragging = true;
        
        const rect = container.getBoundingClientRect();
        updatePosition(touch.clientX - rect.left, touch.clientY - rect.top);
        
        // Simulate Stick Click (L3/R3) on initial touch
        isClicked = true;
        state.buttons1 |= clickMask;
        knob.classList.add('pressed');
        sendState();
    }, { passive: false });

    container.addEventListener('touchmove', (e) => {
        e.preventDefault();
        if (!isDragging) return;

        for (let i = 0; i < e.changedTouches.length; i++) {
            if (e.changedTouches[i].identifier === touchId) {
                const rect = container.getBoundingClientRect();
                updatePosition(e.changedTouches[i].clientX - rect.left, e.changedTouches[i].clientY - rect.top);
                break;
            }
        }
    }, { passive: false });

    const endDrag = (e) => {
        if (!isDragging) return;
        
        let found = false;
        for (let i = 0; i < e.changedTouches.length; i++) {
            if (e.changedTouches[i].identifier === touchId) {
                found = true;
                break;
            }
        }
        if (!found && e.type !== 'touchcancel') return;
        
        e.preventDefault();
        isDragging = false;
        touchId = null;
        
        // Reset position
        knob.style.transform = `translate(-50%, -50%)`;
        if (isLeft) {
            state.lx = 0;
            state.ly = 0;
        } else {
            state.rx = 0;
            state.ry = 0;
        }
        
        // Remove click
        if (isClicked) {
            isClicked = false;
            state.buttons1 &= ~clickMask;
            knob.classList.remove('pressed');
        }

        sendState();
    };

    container.addEventListener('touchend', endDrag, { passive: false });
    container.addEventListener('touchcancel', endDrag, { passive: false });
}

bindJoystick('left-stick-container', true);
bindJoystick('right-stick-container', false);

// Prevent context menus on long press
window.oncontextmenu = function(event) {
    event.preventDefault();
    event.stopPropagation();
    return false;
};

// Initial state send
sendState();
