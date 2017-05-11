$(document).ready(function() {
    $('#systemTable').DataTable({
        aLengthMenu: [
            [-1],
            ["All"]
        ],
        iDisplayLength: -1,
        "paging":   false
    });
});
