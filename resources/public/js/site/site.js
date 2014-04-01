site_remove_kid = function(kid_id, name) {
    if (confirm("Are you sure you want to remove "+name+"?")) {
        $('#kid_id').val(kid_id);
        $('#frm-remove-kid').submit();
    }
};

site_sign_out = function() {
    $.removeCookie('pm[email]');
    $.removeCookie('pm[password]');
    window.location = "/sign-out";
};

site_set_ago = function(newval) {
    $('#activity_dt').val(newval);
    return false;
};

$('#signin').submit(function() {
    var expires_day = 365;
    $.cookie('pm[email]', $('#email').val(), { expires: expires_day });
    $.cookie('pm[password]', $('#password').val(), { expires: expires_day });
    return true;
});

$(document).ready(function() {
    $('#email').val($.cookie('pm[email]'));
    $('#password').val($.cookie('pm[password]'));
    if ($('#email').val() != null && $('#email').val() != '' &&
        $('#password').val() != null && $('#password').val() != '') {
        $('#signin').submit();
    }
});


$(document).ready(function() {
    $('#email').val($.cookie('pm[email]'));
    $('#password').val($.cookie('pm[password]'));
    if ($('#email').val() != null && $('#email').val() != '' &&
        $('#password').val() != null && $('#password').val() != '') {
        $('#signin').submit();
    }
});

$('#save').submit(function() {
    if (!$('input:radio[name="kid_id"]').is(':checked')) {
        alert("You must pick a kid!");
        return false;
    }
    return true;
});
