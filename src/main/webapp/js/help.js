document.addEventListener('DOMContentLoaded', function() {
    console.log('Help page loaded');

    initializeSmoothScrolling();

    if (window.location.hash) {
        setTimeout(() => {
            const element = document.querySelector(window.location.hash);
            if (element) {
                element.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
        }, 100);
    }
});

function toggleAccordion(header) {
    const accordionItem = header.parentElement;
    const content = header.nextElementSibling;
    const arrow = header.querySelector('span');

    const isOpen = accordionItem.classList.contains('active');

    if (isOpen) {

        accordionItem.classList.remove('active');
        content.style.maxHeight = null;
        arrow.style.transform = 'rotate(0deg)';
    } else {

        const accordion = accordionItem.parentElement;
        const allItems = accordion.querySelectorAll('.accordion-item');
        allItems.forEach(item => {
            item.classList.remove('active');
            const itemContent = item.querySelector('.accordion-content');
            const itemArrow = item.querySelector('.accordion-header span');
            if (itemContent) itemContent.style.maxHeight = null;
            if (itemArrow) itemArrow.style.transform = 'rotate(0deg)';
        });

        accordionItem.classList.add('active');
        content.style.maxHeight = content.scrollHeight + 'px';
        arrow.style.transform = 'rotate(90deg)';
    }
}

function initializeSmoothScrolling() {
    const quickLinks = document.querySelectorAll('.quick-link-card');

    quickLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const targetId = this.getAttribute('href');
            const targetElement = document.querySelector(targetId);

            if (targetElement) {

                const headerOffset = 80;
                const elementPosition = targetElement.getBoundingClientRect().top;
                const offsetPosition = elementPosition + window.pageYOffset - headerOffset;

                window.scrollTo({
                    top: offsetPosition,
                    behavior: 'smooth'
                });

                targetElement.classList.add('highlight');
                setTimeout(() => {
                    targetElement.classList.remove('highlight');
                }, 2000);
            }
        });
    });
}

function searchHelpContent(searchTerm) {
    if (!searchTerm || searchTerm.length < 3) {
        return;
    }

    const sections = document.querySelectorAll('.help-section');
    let resultsFound = false;

    sections.forEach(section => {
        const text = section.textContent.toLowerCase();
        const matches = text.includes(searchTerm.toLowerCase());

        if (matches) {
            section.style.display = 'block';
            resultsFound = true;
        } else {
            section.style.display = 'none';
        }
    });

    if (!resultsFound) {
        console.log('No results found for: ' + searchTerm);
    }
}

function printHelpPage() {
    window.print();
}

function expandAllAccordions() {
    const accordionItems = document.querySelectorAll('.accordion-item');
    accordionItems.forEach(item => {
        item.classList.add('active');
        const content = item.querySelector('.accordion-content');
        const arrow = item.querySelector('.accordion-header span');
        if (content) content.style.maxHeight = content.scrollHeight + 'px';
        if (arrow) arrow.style.transform = 'rotate(90deg)';
    });
}

function collapseAllAccordions() {
    const accordionItems = document.querySelectorAll('.accordion-item');
    accordionItems.forEach(item => {
        item.classList.remove('active');
        const content = item.querySelector('.accordion-content');
        const arrow = item.querySelector('.accordion-header span');
        if (content) content.style.maxHeight = null;
        if (arrow) arrow.style.transform = 'rotate(0deg)';
    });
}

window.toggleAccordion = toggleAccordion;
window.expandAllAccordions = expandAllAccordions;
window.collapseAllAccordions = collapseAllAccordions;
window.printHelpPage = printHelpPage;
