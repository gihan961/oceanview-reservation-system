/**
 * Help Page JavaScript
 * Handles accordion functionality, smooth scrolling, and RBAC integration
 */

// Initialize page when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    console.log('Help page loaded');
    
    // Initialize smooth scrolling for quick links
    initializeSmoothScrolling();
    
    // Check if page loaded with a hash (anchor link)
    if (window.location.hash) {
        setTimeout(() => {
            const element = document.querySelector(window.location.hash);
            if (element) {
                element.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
        }, 100);
    }
});

/**
 * Toggle accordion item open/closed
 * @param {HTMLElement} header - The accordion header that was clicked
 */
function toggleAccordion(header) {
    const accordionItem = header.parentElement;
    const content = header.nextElementSibling;
    const arrow = header.querySelector('span');
    
    // Check if this item is currently open
    const isOpen = accordionItem.classList.contains('active');
    
    if (isOpen) {
        // Close this item
        accordionItem.classList.remove('active');
        content.style.maxHeight = null;
        arrow.style.transform = 'rotate(0deg)';
    } else {
        // Close all other accordion items in the same section
        const accordion = accordionItem.parentElement;
        const allItems = accordion.querySelectorAll('.accordion-item');
        allItems.forEach(item => {
            item.classList.remove('active');
            const itemContent = item.querySelector('.accordion-content');
            const itemArrow = item.querySelector('.accordion-header span');
            if (itemContent) itemContent.style.maxHeight = null;
            if (itemArrow) itemArrow.style.transform = 'rotate(0deg)';
        });
        
        // Open this item
        accordionItem.classList.add('active');
        content.style.maxHeight = content.scrollHeight + 'px';
        arrow.style.transform = 'rotate(90deg)';
    }
}

/**
 * Initialize smooth scrolling for quick link cards
 */
function initializeSmoothScrolling() {
    const quickLinks = document.querySelectorAll('.quick-link-card');
    
    quickLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const targetId = this.getAttribute('href');
            const targetElement = document.querySelector(targetId);
            
            if (targetElement) {
                // Scroll to element with offset for header
                const headerOffset = 80;
                const elementPosition = targetElement.getBoundingClientRect().top;
                const offsetPosition = elementPosition + window.pageYOffset - headerOffset;
                
                window.scrollTo({
                    top: offsetPosition,
                    behavior: 'smooth'
                });
                
                // Add highlight effect
                targetElement.classList.add('highlight');
                setTimeout(() => {
                    targetElement.classList.remove('highlight');
                }, 2000);
            }
        });
    });
}

/**
 * Search help content (optional feature for future enhancement)
 * @param {string} searchTerm - The term to search for
 */
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

/**
 * Print help page (optional feature)
 */
function printHelpPage() {
    window.print();
}

/**
 * Expand all accordion items
 */
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

/**
 * Collapse all accordion items
 */
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

// Make functions available globally
window.toggleAccordion = toggleAccordion;
window.expandAllAccordions = expandAllAccordions;
window.collapseAllAccordions = collapseAllAccordions;
window.printHelpPage = printHelpPage;
