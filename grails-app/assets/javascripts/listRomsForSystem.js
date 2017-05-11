$(document).ready(function() {
    $('#romTable').DataTable({
        aLengthMenu: [
            [-1],
            ["All"]
        ],
        iDisplayLength: -1,
        aoColumnDefs: [
            {
                // Custom rendering for the first column
                // which is filename over scrape name
                "mRender": function ( data, type, row ) {
                    return data +'<br/>' + row[1];
                },
                "aTargets": [ 0 ]
            },
            { "bVisible": false,  "aTargets": [ 1 ] }
        ],
        "paging":   false
    });
});
