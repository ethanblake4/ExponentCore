/**
 * This script is part of ExponentCore. github.com/ethanblake4/ExponentCore
 * Please do not modify or reuse it without credit in the source code.
 *
 * AuthFlow.js v1
 * To be injected via WebView.evaluateJavascript on the Google AddSession page only.
 * Attaches to input fields and buttons and returns username and password values
 * through an Android JavascriptInterface object.
 */
function getPass() {
    let pass = Array.from(document.getElementsByTagName('input')).find((i1)=>i1.type=="password");
    if(!pass) {
        console.log("Set pass timeout");
        setTimeout(getPass, 50);
    } else {
        console.log("Got pass");
        getNx();
    }
}
function exReturn() {
    let pass = Array.from(document.getElementsByTagName('input')).find((i1)=>i1.type=="password");
    let passwd = pass.value;
    Android.returnPassword(passwd);
    document.getElementsByClassName('qdulke')[0].classList.remove('qdulke');
    prog();
}
function prog() {
    let jk = document.getElementsByClassName('jK7moc');
    if(jk.length > 0) jk[0].classList.remove('jK7moc');
    setTimeout(prog, 100);
}
function getNx() {
    let nx = document.getElementById('passwordNext');
    if(!nx) {
        setTimeout(getNx, 50);
    } else {
        nx.__jsaction = {};
        nx.__jscontroller = {};
        console.log(nx);
        document.getElementById('passwordNext').addEventListener('click', function(event) {
            event.preventDefault();
            exReturn();
        });
    }
}
document.getElementById('identifierNext').addEventListener('click', function(ev) {
    Android.returnUsername((document.getElementById('identifierId').value));
    setTimeout(getPass, 100);
});