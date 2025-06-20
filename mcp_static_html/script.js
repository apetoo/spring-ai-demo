// 导航栏滚动效果
window.addEventListener('scroll', function() {
    const navbar = document.getElementById('navbar');
    const scrollPosition = window.scrollY;
    
    if (scrollPosition > 50) {
        navbar.style.padding = '10px 0';
        navbar.style.backgroundColor = 'rgba(15, 23, 42, 0.95)';
    } else {
        navbar.style.padding = '15px 0';
        navbar.style.backgroundColor = 'rgba(15, 23, 42, 0.9)';
    }
    
    // 更新侧边导航激活状态
    updateSideNav();
    
    // 更新导航链接激活状态
    updateNavLinks();
    
    // 检查元素可见性并添加动画
    checkVisibility();
});

// 侧边导航激活状态
function updateSideNav() {
    const sections = ['hero', 'ai-concepts', 'definition', 'mechanism', 'tools', 'practical'];
    const scrollPosition = window.scrollY + window.innerHeight / 3;
    
    for (const section of sections) {
        const element = document.getElementById(section);
        if (element) {
            const { offsetTop, offsetHeight } = element;
            const sideDot = document.querySelector(`.side-dot[data-section="${section}"]`);
            
            if (scrollPosition >= offsetTop && scrollPosition < offsetTop + offsetHeight) {
                if (sideDot && !sideDot.classList.contains('active')) {
                    document.querySelectorAll('.side-dot').forEach(dot => dot.classList.remove('active'));
                    sideDot.classList.add('active');
                }
                break;
            }
        }
    }
}

// 导航链接激活状态
function updateNavLinks() {
    const sections = ['ai-concepts', 'definition', 'mechanism', 'tools', 'practical'];
    const scrollPosition = window.scrollY + window.innerHeight / 3;
    
    for (const section of sections) {
        const element = document.getElementById(section);
        if (element) {
            const { offsetTop, offsetHeight } = element;
            const navLink = document.querySelector(`.nav-link[href="#${section}"]`);
            
            if (scrollPosition >= offsetTop && scrollPosition < offsetTop + offsetHeight) {
                if (navLink && !navLink.classList.contains('active')) {
                    document.querySelectorAll('.nav-link').forEach(link => link.classList.remove('active'));
                    navLink.classList.add('active');
                }
                break;
            }
        }
    }
}

// 平滑滚动
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function(e) {
        e.preventDefault();
        
        const targetId = this.getAttribute('href');
        const targetElement = document.querySelector(targetId);
        
        if (targetElement) {
            window.scrollTo({
                top: targetElement.offsetTop,
                behavior: 'smooth'
            });
            
            // 如果是移动设备上的菜单点击，关闭菜单
            if (window.innerWidth <= 768 && this.classList.contains('nav-link')) {
                toggleMobileMenu();
            }
        }
    });
});

// 移动端菜单切换
const menuToggle = document.querySelector('.menu-toggle');
const navLinks = document.querySelector('.nav-links');

function toggleMobileMenu() {
    menuToggle.classList.toggle('active');
    
    if (!navLinks.style.display || navLinks.style.display === 'none') {
        navLinks.style.display = 'flex';
        navLinks.style.flexDirection = 'column';
        navLinks.style.position = 'absolute';
        navLinks.style.top = '100%';
        navLinks.style.left = '0';
        navLinks.style.width = '100%';
        navLinks.style.backgroundColor = 'rgba(15, 23, 42, 0.95)';
        navLinks.style.padding = '20px';
        navLinks.style.boxShadow = '0 10px 20px rgba(0, 0, 0, 0.2)';
    } else {
        navLinks.style.display = 'none';
    }
}

if (menuToggle) {
    menuToggle.addEventListener('click', toggleMobileMenu);
}

// 检查元素可见性并添加动画
function checkVisibility() {
    const fadeElements = document.querySelectorAll('.fade-in:not(.visible)');
    const windowHeight = window.innerHeight;
    const scrollPosition = window.scrollY;
    
    fadeElements.forEach(element => {
        const elementPosition = element.getBoundingClientRect().top + scrollPosition;
        
        if (scrollPosition + windowHeight > elementPosition + 100) {
            element.classList.add('visible');
        }
    });
}

// 页面加载时添加淡入动画
window.addEventListener('DOMContentLoaded', function() {
    // 为所有需要动画的元素添加fade-in类
    const animatedElements = [
        '.section-header',
        '.flip-card',
        '.mechanism-block',
        '.mechanism-text',
        '.mechanism-image',
        '.architecture-box',
        '.workflow-step',
        '.deployment-mode',
        '.ide-card',
        '.tool-item',
        '.workflow-item',
        '.ecosystem-card',
        '.practical-block',
        '.step-item',
        '.code-container',
        '.scenario-card',
        '.tips-box'
    ];
    
    animatedElements.forEach(selector => {
        document.querySelectorAll(selector).forEach((element, index) => {
            element.classList.add('fade-in');
            // 为元素添加延迟，创建级联效果
            element.style.transitionDelay = `${index * 0.1}s`;
        });
    });
    
    // 初始检查可见元素
    checkVisibility();
    
    // 初始化侧边导航和导航链接
    updateSideNav();
    updateNavLinks();
});

// 卡片翻转效果
document.querySelectorAll('.flip-card').forEach(card => {
    card.addEventListener('click', function() {
        this.classList.toggle('flipped');
    });
});

// Tooltip 动态定位
document.querySelectorAll('.tooltip-trigger').forEach(trigger => {
    const tooltip = trigger.querySelector('.tooltip-content');
    if (!tooltip) return;

    const originalParent = tooltip.parentNode;
    let isTooltipInBody = false; // Flag to track if tooltip is currently in body

    trigger.addEventListener('mouseenter', () => {
        // Ensure tooltip is appended to body only once if not already there
        if (!isTooltipInBody) {
            document.body.appendChild(tooltip);
            isTooltipInBody = true;
        }

        // Apply styles to make it visible and start animation
        tooltip.style.visibility = 'visible'; // Set visible first
        tooltip.style.opacity = '1';
        tooltip.style.transform = 'scale(1) translateY(0)';

        // Calculate position after styles are applied and element is in the DOM flow
        requestAnimationFrame(() => {
            const triggerRect = trigger.getBoundingClientRect();
            const tooltipRect = tooltip.getBoundingClientRect();

            let top = triggerRect.bottom + window.scrollY + 10; 
            let left = triggerRect.left + window.scrollX + (triggerRect.width / 2) - (tooltipRect.width / 2);

            if (left < 10) left = 10;
            if (left + tooltipRect.width > window.innerWidth - 10) {
                left = window.innerWidth - tooltipRect.width - 10;
            }
            if (triggerRect.bottom + tooltipRect.height + 20 > window.innerHeight && triggerRect.top - tooltipRect.height - 10 > 0) {
                top = triggerRect.top + window.scrollY - tooltipRect.height - 10;
            } else if (triggerRect.bottom + tooltipRect.height + 20 > window.innerHeight) {
                 // If not enough space above or below, try to center vertically if possible or just place below
                top = Math.max(10, window.scrollY + (window.innerHeight - tooltipRect.height) / 2); 
            }

            tooltip.style.top = `${top}px`;
            tooltip.style.left = `${left}px`;
            tooltip.style.pointerEvents = 'auto'; // Make tooltip interactive after positioning
        });
    });

    trigger.addEventListener('mouseleave', () => {
        tooltip.style.opacity = '0';
        tooltip.style.transform = 'scale(0.95) translateY(10px)';
        tooltip.style.pointerEvents = 'none'; // Make tooltip non-interactive immediately
        
        // After animation, hide and move back to original parent
        setTimeout(() => {
            if (isTooltipInBody && originalParent && tooltip.parentNode === document.body) {
                originalParent.appendChild(tooltip);
                isTooltipInBody = false;
            }
            tooltip.style.visibility = 'hidden'; // Hide after re-attaching and animation
        }, 300); // Match CSS transition duration
    });
});

// 响应式调整
window.addEventListener('resize', function() {
    if (window.innerWidth > 768 && navLinks.style.display === 'flex' && navLinks.style.flexDirection === 'column') {
        navLinks.style.display = '';
        navLinks.style.flexDirection = '';
        navLinks.style.position = '';
        navLinks.style.top = '';
        navLinks.style.left = '';
        navLinks.style.width = '';
        navLinks.style.backgroundColor = '';
        navLinks.style.padding = '';
        navLinks.style.boxShadow = '';
    }
});
