$(document).ready(function() {
    $('#systemTable').DataTable({
        searchHighlight: true,
        aLengthMenu: [
            [-1],
            ["All"]
        ],
        iDisplayLength: -1,
        "paging":   false
    });
});
