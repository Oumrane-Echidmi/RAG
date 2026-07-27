/* ============================================================
   Oumrane Echidmi — AI Portfolio
   Chat Application JavaScript
   ============================================================ */

(function () {
    'use strict';

    // ---------- DOM References ----------
    const chatContainer = document.getElementById('chatContainer');
    const messagesArea  = document.getElementById('messagesArea');
    const typingEl      = document.getElementById('typingIndicator');
    const welcomeCard   = document.getElementById('welcomeCard');
    const chipsContainer = document.getElementById('chipsContainer');
    const messageInput  = document.getElementById('messageInput');
    const sendButton    = document.getElementById('sendButton');

    // ---------- State ----------
    let isWaiting = false;

    // ---------- Initialisation ----------
    init();

    function init() {
        bindEvents();
        // Autofocus on desktop (avoid keyboard pop on mobile)
        if (window.innerWidth > 768) {
            messageInput.focus();
        }
    }

    // ---------- Event Binding ----------
    function bindEvents() {
        // Send button
        sendButton.addEventListener('click', handleSend);

        // Enter to send, Shift+Enter for new line
        messageInput.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                handleSend();
            }
        });

        // Auto-resize textarea
        messageInput.addEventListener('input', autoResizeTextarea);

        // Quick chips
        chipsContainer.addEventListener('click', function (e) {
            const chip = e.target.closest('.chip');
            if (!chip) return;
            const question = chip.dataset.question;
            if (question) {
                messageInput.value = question;
                handleSend();
            }
        });
    }

    // ---------- Send Handler ----------
    async function handleSend() {
        if (isWaiting) return;

        const message = messageInput.value.trim();
        if (!message) return;

        // Hide welcome card on first interaction
        if (welcomeCard && !welcomeCard.hidden) {
            welcomeCard.style.display = 'none';
            welcomeCard.hidden = true;
        }

        // Append user message
        appendMessage('user', message);

        // Clear input & reset height
        messageInput.value = '';
        autoResizeTextarea();
        messageInput.focus();

        // Show typing indicator
        showTyping(true);
        isWaiting = true;
        sendButton.disabled = true;

        try {
            const response = await fetch('/api/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message: message })
            });

            if (!response.ok) {
                throw new Error(`Server responded with ${response.status}`);
            }

            const data = await response.json();
            showTyping(false);
            appendMessage('ai', data.answer || 'I received your message but could not generate a response.');

        } catch (err) {
            console.error('Chat error:', err);
            showTyping(false);
            showError('Could not reach the assistant. Please try again.');
        } finally {
            isWaiting = false;
            sendButton.disabled = false;
            messageInput.focus();
        }
    }

    // ---------- Message Rendering ----------
    function appendMessage(role, text) {
        const wrapper = document.createElement('div');
        wrapper.className = `message ${role}`;

        // Avatar
        const avatar = document.createElement('div');
        avatar.className = 'message-avatar';
        if (role === 'ai') {
            avatar.innerHTML = `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3Z"/>
                <path d="M19 10v2a7 7 0 0 1-14 0v-2"/>
                <line x1="12" x2="12" y1="19" y2="22"/>
            </svg>`;
        } else {
            avatar.textContent = 'Y';
        }

        // Content
        const content = document.createElement('div');
        content.className = 'message-content';

        if (role === 'ai') {
            content.innerHTML = renderMarkdown(text);
        } else {
            content.textContent = text;
        }

        wrapper.appendChild(avatar);
        wrapper.appendChild(content);
        messagesArea.appendChild(wrapper);

        // Scroll to bottom
        scrollToBottom();
    }

    // ---------- Simple Markdown Renderer ----------
    function renderMarkdown(text) {
        if (!text) return '';

        // Escape HTML first
        let html = escapeHtml(text);

        // Code blocks (``` ... ```)
        html = html.replace(/```(\w*)\n?([\s\S]*?)```/g, function (match, lang, code) {
            return '<pre><code>' + code.trim() + '</code></pre>';
        });

        // Inline code (`...`)
        html = html.replace(/`([^`]+)`/g, '<code>$1</code>');

        // Bold (**text** or __text__)
        html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
        html = html.replace(/__([^_]+)__/g, '<strong>$1</strong>');

        // Italic (*text* or _text_)
        html = html.replace(/\*([^*]+)\*/g, '<em>$1</em>');
        html = html.replace(/_([^_]+)_/g, '<em>$1</em>');

        // Blockquote (> text)
        html = html.replace(/^&gt;\s?(.*)$/gm, '<blockquote>$1</blockquote>');

        // Unordered list items (- or * at start of line)
        html = html.replace(/^[\-\*]\s+(.*)$/gm, '<li>$1</li>');
        // Wrap consecutive <li> in <ul>
        html = html.replace(/((?:<li>.*<\/li>\n?)+)/g, '<ul>$1</ul>');

        // Ordered list items (1. 2. etc.)
        html = html.replace(/^\d+\.\s+(.*)$/gm, '<li>$1</li>');

        // Headings
        html = html.replace(/^### (.+)$/gm, '<h3>$1</h3>');
        html = html.replace(/^## (.+)$/gm, '<h2>$1</h2>');
        html = html.replace(/^# (.+)$/gm, '<h1>$1</h1>');

        // Horizontal rule
        html = html.replace(/^---$/gm, '<hr>');

        // Links [text](url)
        html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener">$1</a>');

        // Paragraphs: wrap text blocks in <p>
        // Split by double newline, wrap each block
        const blocks = html.split(/\n\n+/);
        html = blocks.map(function (block) {
            block = block.trim();
            if (!block) return '';
            // Don't wrap if already a block element
            if (/^<(pre|ul|ol|blockquote|h[1-6]|hr|li)/.test(block)) {
                return block;
            }
            return '<p>' + block.replace(/\n/g, '<br>') + '</p>';
        }).join('\n');

        return html;
    }

    function escapeHtml(str) {
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#39;'
        };
        return str.replace(/[&<>"']/g, function (c) { return map[c]; });
    }

    // ---------- Typing Indicator ----------
    function showTyping(show) {
        typingEl.style.display = show ? 'flex' : 'none';
        scrollToBottom();
    }

    // ---------- Scroll Helper ----------
    function scrollToBottom() {
        requestAnimationFrame(function () {
            chatContainer.scrollTop = chatContainer.scrollHeight;
        });
    }

    // ---------- Auto-resize Textarea ----------
    function autoResizeTextarea() {
        messageInput.style.height = 'auto';
        messageInput.style.height = Math.min(messageInput.scrollHeight, 120) + 'px';
    }

    // ---------- Error Toast ----------
    function showError(msg) {
        // Remove any existing toast
        const existing = document.querySelector('.error-toast');
        if (existing) existing.remove();

        const toast = document.createElement('div');
        toast.className = 'error-toast';
        toast.textContent = msg;
        document.body.appendChild(toast);

        setTimeout(function () {
            toast.style.animation = 'toastOut 0.3s ease forwards';
            setTimeout(function () { toast.remove(); }, 300);
        }, 3000);
    }
})();
