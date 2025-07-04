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
document.addEventListener('DOMContentLoaded', function() {
    const flipCards = document.querySelectorAll('.flip-card');
    
    flipCards.forEach(card => {
        card.addEventListener('click', function() {
            this.classList.toggle('flipped');
        });
    });
});

// Lightbox 效果
document.addEventListener('DOMContentLoaded', function() {
    const lightbox = document.getElementById('lightbox-modal');
    const lightboxImage = document.getElementById('lightbox-image');
    const lightboxContainer = document.querySelector('.lightbox-image-container');
    const images = document.querySelectorAll('.card-image');
    const closeBtn = document.querySelector('.close-lightbox');
    const zoomInBtn = document.getElementById('zoom-in');
    const zoomOutBtn = document.getElementById('zoom-out');
    const zoomResetBtn = document.getElementById('zoom-reset');
    
    let currentScale = 1;
    let isDragging = false;
    let startX, startY, translateX = 0, translateY = 0;

    images.forEach(image => {
        image.addEventListener('click', function(e) {
            e.stopPropagation(); // 防止触发卡片翻转
            lightbox.style.display = 'block';
            lightboxImage.src = this.src;
            resetImagePosition();
        });
    });

    function resetImagePosition() {
        currentScale = 1;
        translateX = 0;
        translateY = 0;
        updateImageTransform();
    }

    function updateImageTransform() {
        lightboxImage.style.transform = `scale(${currentScale}) translate(${translateX}px, ${translateY}px)`;
    }

    function zoomImage(factor) {
        const newScale = currentScale * factor;
        if (newScale >= 0.5 && newScale <= 5) {
            currentScale = newScale;
            updateImageTransform();
        }
    }

    function closeLightbox() {
        lightbox.style.display = 'none';
        resetImagePosition();
    }

    // 缩放按钮事件
    if(zoomInBtn) {
        zoomInBtn.addEventListener('click', () => zoomImage(1.2));
    }

    if(zoomOutBtn) {
        zoomOutBtn.addEventListener('click', () => zoomImage(0.8));
    }

    if(zoomResetBtn) {
        zoomResetBtn.addEventListener('click', resetImagePosition);
    }

    // 鼠标滚轮缩放
    if(lightboxContainer) {
        lightboxContainer.addEventListener('wheel', function(e) {
            e.preventDefault();
            const zoomFactor = e.deltaY > 0 ? 0.9 : 1.1;
            zoomImage(zoomFactor);
        });
    }

    // 拖拽功能
    if(lightboxImage) {
        lightboxImage.addEventListener('mousedown', function(e) {
            if (currentScale > 1) {
                isDragging = true;
                startX = e.clientX - translateX;
                startY = e.clientY - translateY;
                lightboxImage.style.cursor = 'grabbing';
            }
        });

        document.addEventListener('mousemove', function(e) {
            if (isDragging) {
                translateX = e.clientX - startX;
                translateY = e.clientY - startY;
                updateImageTransform();
            }
        });

        document.addEventListener('mouseup', function() {
            isDragging = false;
            lightboxImage.style.cursor = currentScale > 1 ? 'grab' : 'default';
        });
    }

    if(closeBtn) {
        closeBtn.addEventListener('click', closeLightbox);
    }

    if(lightbox) {
        lightbox.addEventListener('click', function(e) {
            if (e.target === lightbox) {
                closeLightbox();
            }
        });
    }
});

