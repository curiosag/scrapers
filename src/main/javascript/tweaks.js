/**
 * context menu observer to determine current coordinates
 * */

((() => {
    const mobserver = new MutationObserver(function (mutations) {
        var menuentry = document.querySelector('.mapsTactileClientActionmenu__action-menu-entry-text');
        if(menuentry)
           console.log(menuentry.innerText)
    });

    mobserver.observe(document.getElementById('hovercard'), {attributes: true, childList: true, subtree: true});
}))();