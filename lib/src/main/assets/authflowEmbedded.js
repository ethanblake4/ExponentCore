function getPass() {
    let pass = Array.from(document.getElementsByTagName('input')).find((i1)=>i1.type=="password");
    if(!pass) {
        setTimeout(getPass, 50);
    } else {
        getPass2();
        getNx();
    }
}
function getPass2() {
    let pass2 = document.getElementById('password');
    if(!pass2) setTimeout(getPass2, 50);
    else {
        pass2.autocomplete = "current-password";
        pass2.parentElement.addEventListener('keydown', function(ev) {
            if(ev.key == "Enter") clickAgree();
        });
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

function clickAgree() {
    window.agreeTimes++;
    if(window.agreeTimes >= 15) {
       addVisOverlay();
    }
    let relevantClassList = document.getElementsByClassName('qIypjc');

    if(relevantClassList.length < 1) {
        setTimeout(clickAgree, 50);
    } else {
        let activeButton = relevantClassList[0];
        let txt = activeButton.innerText;
        if(!window.buttonText || window.buttonText == txt) {
            window.buttonText = txt;
            setTimeout(clickAgree, 50);
            console.log("txt: " + txt);
        } else {
            window.buttonText = txt;
            addVisOverlay();
            activeButton.click();
            Android.returnPassword('');
            console.log("click");
        }
    }
}

function addVisOverlay() {
    let ovr = document.createElement('div');
    ovr.style.position = 'fixed';
    ovr.style.width = '100%';
    ovr.style.height = '100%';
    ovr.style.top = 0;
    ovr.style.left = 0;
    ovr.style.zIndex = 1000;
    ovr.style.backgroundColor = 'white';
    document.body.appendChild(ovr);
}

function getNx() {
    let nx = document.getElementById('passwordNext');
    if(!nx) {
        setTimeout(getNx, 50);
    } else {
        document.getElementById('passwordNext').addEventListener('click', function(event) {
            clickAgree();
        });
    }
}

if(!document.exAuthFlow) {
    window.buttonText = false;
    window.agreeTimes = 0;
    console.log("Initialized AuthFlow");
    document.getElementById('initialView').style.zIndex = 10;
    document.getElementById('initialView').style.paddingTop = 0;
    document.getElementsByClassName('c8DD0')[0].style.top = 0;
    function settr(ev) {
        console.log(document.getElementById('identifierId'));
        Android.returnUsername((document.getElementById('identifierId').value));
        setTimeout(getPass, 100);
    };
    document.getElementById('identifierNext').addEventListener('touchend', settr);
    document.getElementById('identifierNext').addEventListener('click', settr);
    document.getElementById('identifierId').addEventListener('keydown', function(ev) {
        if(ev.key == "Enter") settr(ev);
    });
    document.getElementById('identifierId').autocomplete = "email";
    document.getElementById("learnMore").style.display = "none"
    document.exAuthFlow = true;
}