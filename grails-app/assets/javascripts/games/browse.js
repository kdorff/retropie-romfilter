$(document).on('click', 'button.deleteRom', function (e) {
    e.preventDefault();

    var button = $(this);
    var deleteUrl = button.data('delete-url');
    if (deleteUrl) {
        button.css("background", "yellow");
        button.html('Attempting to Move ROM to Trash');
        $.ajax({
            url: deleteUrl,
            type: 'DELETE',
            success: function (result) {
                button.html('ROM Moved To Trash');
                button.prop('disabled', true);
                button.css("background", "chartreuse");
            },
            error: function (result) {
                button.css("background", "");
                button.html('Delete / Move Trash');
                alert('Error deleting the ROM.');
            }
        });
    }
});

$(document).on('click', 'a.toggle-vis', function (e) {
    e.preventDefault();

    // Get the column API object
    var column = romTable.column( $(this).attr('data-column') );

    // Toggle the visibility
    column.visible( ! column.visible() );
});
