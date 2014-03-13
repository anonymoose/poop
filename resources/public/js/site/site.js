$(function () {

});


site_remove_kid = function(kid_id, name) {
    if (confirm("Are you sure you want to remove "+name+"?")) {
        $('#kid_id').val(kid_id);
        $('#frm-remove-kid').submit();
    }
};
