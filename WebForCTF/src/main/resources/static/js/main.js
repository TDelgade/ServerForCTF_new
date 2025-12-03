/**
 * CTF Platform - Main JavaScript
 * Инициализация приложения и общая логика
 */

class CTFPlatform {
    constructor() {
        this.currentPage = window.location.pathname;
        this.init();
    }

    init() {
        console.log('🚀 CTF Platform initialized on:', this.currentPage);
        this.initNavigation();
        this.initTheme();
        this.initParticles();
        this.initScrollEffects();
        this.initEventListeners();
        this.initPageSpecific();
    }

    initNavigation() {
        const currentPath = window.location.pathname;
        const navLinks = document.querySelectorAll('.nav-link');

        navLinks.forEach(link => {
            const linkPath = link.getAttribute('href');
            const isLoginBtn = link.classList.contains('login-btn');

            link.classList.remove('active');

            if (isLoginBtn) {
                if (currentPath === '/auth') link.classList.add('active');
            } else if (linkPath) {
                if (currentPath === linkPath) link.classList.add('active');
                else if (currentPath.startsWith('/category/') && linkPath === currentPath) link.classList.add('active');
                else if (currentPath.startsWith('/challenges/') && linkPath === currentPath) link.classList.add('active');
                else if (currentPath === '/' && linkPath === '/') link.classList.add('active');
                else if (currentPath === '/users' && linkPath === '/users') link.classList.add('active');
            }
        });
    }

    initTheme() {
        const savedTheme = localStorage.getItem('ctf-theme');
        if (savedTheme) document.documentElement.setAttribute('data-theme', savedTheme);
    }

    initParticles() {
        const particlesContainer = document.querySelector('.particles');
        if (!particlesContainer) return;
        particlesContainer.innerHTML = '';

        const particleCount = 8;
        for (let i = 0; i < particleCount; i++) this.createParticle(particlesContainer, i);
    }

    createParticle(container, index) {
        const particle = document.createElement('div');
        particle.className = 'particle';

        const size = Math.random() * 6 + 2;
        const posX = Math.random() * 100;
        const posY = Math.random() * 100;
        const delay = Math.random() * 5;
        const duration = Math.random() * 10 + 5;

        Object.assign(particle.style, {
            width: `${size}px`,
            height: `${size}px`,
            top: `${posY}%`,
            left: `${posX}%`,
            animationDelay: `${delay}s`,
            animationDuration: `${duration}s`,
            background: index % 3 === 0 ? 'var(--primary-color)' :
                       index % 3 === 1 ? 'var(--secondary-color)' : 'var(--accent-color)'
        });

        container.appendChild(particle);
    }

    initScrollEffects() {
        const background = document.querySelector('.background');
        if (background) {
            background._mouseMoveHandler && document.removeEventListener('mousemove', background._mouseMoveHandler);
            background._mouseMoveHandler = (e) => {
                const x = e.clientX / window.innerWidth;
                const y = e.clientY / window.innerHeight;
                background.style.transform = `translate(${x * 20}px, ${y * 20}px)`;
            };
            window.addEventListener('mousemove', background._mouseMoveHandler);
        }

        document.querySelectorAll('a[href^="#"]').forEach(anchor => {
            anchor.addEventListener('click', function (e) {
                e.preventDefault();
                const target = document.querySelector(this.getAttribute('href'));
                if (target) target.scrollIntoView({ behavior: 'smooth', block: 'start' });
            });
        });
    }

    initEventListeners() {
        document.addEventListener('click', (e) => {
            const challengeCard = e.target.closest('.challenge-card');
            if (challengeCard && challengeCard.getAttribute('data-href')) {
                e.preventDefault();
                window.location.href = challengeCard.getAttribute('data-href');
            }
        });
    }

    initPageSpecific() {
        const path = window.location.pathname;
        if (path === '/') this.initHomePage();
        else if (path === '/users') this.initUsersPage();
        else if (path.includes('/category/')) this.initCategoryPage();
        else if (path === '/auth') this.initAuthPage();
        else if (path.includes('/challenges/')) this.initChallengePage();
    }

    initHomePage() {
        this.initTerminal();
        this.initLeaderboard();
        this.initCategoryCards();
    }

    initTerminal() {
        const terminalBody = document.querySelector('.terminal-body');
        if (!terminalBody) return;

        const messages = [
            "> Scanning network infrastructure...",
            "> Firewall detected: BYPASSING...",
            "> Access granted to mainframe...",
            "> Loading exploit database...",
            "> System fully operational...",
            "> Welcome, hacker. Ready for challenges?"
        ];

        let currentMessage = 0;
        let currentChar = 0;
        let isDeleting = false;
        const typingSpeed = 50;

        const typeWriter = () => {
            if (currentMessage < messages.length) {
                const currentText = messages[currentMessage];
                if (!isDeleting && currentChar <= currentText.length) {
                    terminalBody.innerHTML = this.getCurrentText(messages, currentMessage) +
                        currentText.substring(0, currentChar) + '<span class="blink">|</span>';
                    currentChar++;
                    setTimeout(typeWriter, typingSpeed);
                } else if (isDeleting && currentChar >= 0) {
                    terminalBody.innerHTML = this.getCurrentText(messages, currentMessage) +
                        currentText.substring(0, currentChar) + '<span class="blink">|</span>';
                    currentChar--;
                    setTimeout(typeWriter, typingSpeed / 2);
                } else {
                    isDeleting = !isDeleting;
                    if (!isDeleting) {
                        currentMessage++;
                        currentChar = 0;
                    }
                    setTimeout(typeWriter, typingSpeed * 10);
                }
            } else {
                terminalBody.innerHTML = this.getCurrentText(messages, currentMessage) + '<span class="blink">_</span>';
            }
        };

        setTimeout(typeWriter, 1000);
    }

    getCurrentText(messages, currentMessage) {
        let text = '';
        for (let i = 0; i < currentMessage; i++) text += messages[i] + '<br>';
        return text;
    }

    initLeaderboard() {
        console.log('initLeaderboard called');
        generateTop3Leaderboard();
    }

//   generateTop3Leaderboard() {
//       const top3List = document.getElementById('leaderboardTop3');
//       if (!top3List) return;
//
//       top3List.innerHTML = '';
//       console.log("Загрузка топ-3 пользователей...");
//
//       fetch('http://localhost:8081/top3')
//           .then(res => res.json())
//           .then(users => {
//               console.log("Top3 users raw data:", users); // <-- добавь эту строку
//
//               if (!users || users.length === 0) {
//                   top3List.innerHTML = '<div class="no-users-message">Нет данных о пользователях</div>';
//                   return;
//               }
//
//               const topUsers = users.slice(0, 3);
//               topUsers.forEach((user, index) => {
//                   console.log("User object:", user); // <-- и эту, чтобы точно видеть все поля
//
//                   const leaderItem = document.createElement('div');
//                   leaderItem.className = `leader-item`;
//                   leaderItem.style.animationDelay = `${index * 0.2}s`;
//
//                   leaderItem.innerHTML = `
//                       <div class="leader-rank">${index + 1}</div>
//                       <div class="leader-info">
//                           <div class="leader-name">${user.login}</div>
//                           <div class="leader-stats">
//                               Points: ${user.points ?? 0} | Lab Points: ${user.pointsLab ?? 0}
//                           </div>
//                       </div>
//                   `;
//
//                   top3List.appendChild(leaderItem);
//               });
//           })
//           .catch(err => {
//               console.error('Ошибка при загрузке топ-3:', err);
//               top3List.innerHTML = '<div class="no-users-message">Ошибка загрузки данных</div>';
//           });
//   }



    initCategoryCards() {
        const cards = document.querySelectorAll('.category-card, .challenge-card');
        cards.forEach(card => {
            card._mouseEnterHandler && card.removeEventListener('mouseenter', card._mouseEnterHandler);
            card._mouseLeaveHandler && card.removeEventListener('mouseleave', card._mouseLeaveHandler);

            card._mouseEnterHandler = () => card.style.transform = 'translateY(-10px)';
            card._mouseLeaveHandler = () => card.style.transform = 'translateY(0)';

            card.addEventListener('mouseenter', card._mouseEnterHandler);
            card.addEventListener('mouseleave', card._mouseLeaveHandler);
        });
    }

    initUsersPage() {
        console.log('Initializing users page');
    }

    initCategoryPage() {
        this.initCategoryCards();
    }

    initAuthPage() {
        const form = document.querySelector('#loginForm'); // либо правильный ID формы
        if (!form) return;

        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(form);

            try {
                const response = await fetch('/login', { method: 'POST', body: formData, credentials: 'include' });
                const data = await response.json();
                if (data.success) {
                    window.location.href = data.redirect;
                } else {
                    CTFPlatform.showNotification(data.error, "error");
                }
            } catch (error) {
                console.error(error);
                CTFPlatform.showNotification("Ошибка соединения", "error");
            }
        });
    }



    initChallengePage() {
        console.log('Initializing challenge page');
    }

    /* ==============================
       ✅ ПРОВЕРКА ПРОМОКОДОВ
    ============================== */
    async checkPromoCode() {
        const promoInput = document.getElementById("promoInput");
        const promoCode = promoInput?.value.trim();
        const messageEl = document.getElementById("promoMessage");

        if (messageEl) messageEl.innerText = "";

        if (!promoCode) {
            if (messageEl) messageEl.innerText = "Введите промокод";
            return;
        }

        try {
            // Отправляем JSON, CORS фильтр теперь позволяет это
            const promoRes = await fetch("http://localhost:8081/promo/use", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ code: promoCode }),
                credentials: "include"
            });

            const promoData = await promoRes.json();

            if (promoData.success) {
                const login = localStorage.getItem("login");

                if (!login) {
                    if (messageEl) messageEl.innerText = "Пользователь не найден";
                    return;
                }

                // Начисление баллов
                const pointsRes = await fetch(
                    `http://localhost:8081/users/${login}/points/add/${promoData.points}`,
                    {
                        method: "POST",
                        credentials: "include"
                    }
                );

                if (pointsRes.ok) {
                    if (messageEl)
                        messageEl.innerText = `Промокод активирован! +${promoData.points} баллов`;
                } else {
                    if (messageEl) messageEl.innerText = "Ошибка при начислении баллов";
                }

            } else {
                if (messageEl)
                    messageEl.innerText = promoData.message || "Промокод недействителен";
            }

        } catch (err) {
            console.error(err);
            if (messageEl) messageEl.innerText = "Ошибка при проверке";
        }
    }

}
/* ==============================
   ИНИЦИАЛИЗАЦИЯ ПЛАТФОРМЫ
============================== */
let ctfPlatformInstance = null;

function initializeCTFPlatform() {
    ctfPlatformInstance = new CTFPlatform();
}

document.addEventListener('DOMContentLoaded', initializeCTFPlatform);
window.addEventListener('popstate', initializeCTFPlatform);

/* ==============================
   ГЛОБАЛЬНАЯ ФУНКЦИЯ ПРОМОКОДА
============================== */
window.checkPromoCode = () => {
    if (ctfPlatformInstance) ctfPlatformInstance.checkPromoCode();
};

/* ==============================
   ГЛОБАЛЬНЫЕ УТИЛИТЫ
============================== */
window.CTFPlatform = {
    showNotification: (message, type = 'info') => {
        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        notification.textContent = message;

        Object.assign(notification.style, {
            position: 'fixed',
            top: '100px',
            right: '20px',
            padding: '1rem 1.5rem',
            borderRadius: '8px',
            background: 'var(--background-card)',
            border: `1px solid var(--${type}-color)`,
            boxShadow: '0 5px 20px rgba(0, 0, 0, 0.3)',
            zIndex: '10000',
            animation: 'slideIn 0.3s ease-out'
        });

        document.body.appendChild(notification);

        setTimeout(() => notification.remove(), 3000);
    },

    debounce: (func, wait) => {
        let timeout;
        return function executedFunction(...args) {
            const later = () => { clearTimeout(timeout); func(...args); };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }
};
document.addEventListener('DOMContentLoaded', () => {
    const btn = document.getElementById('promoBtn');
    btn?.addEventListener('click', () => {
        if (ctfPlatformInstance) ctfPlatformInstance.checkPromoCode();
    });
});