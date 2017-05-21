$(document).on('click', '.deleteRom', function () {
    var button = $(this);
    var deleteUrl = button.data('delete-url');
    if (deleteUrl) {
        button.css("background", "yellow");
        button.attr('value', 'Attempting to Move ROM to Trash');
        $.ajax({
            url: deleteUrl,
            type: 'DELETE',
            success: function (result) {
                button.attr('value', 'ROM Moved To Trash');
                button.prop('disabled', true);
                button.css("background", "green");
                console.log("ROM deleted");
            },
            error: function (result) {
                button.css("background", "");
                button.attr('value', 'Delete / Move Trash');
                alert('Error deleting the ROM.');
            }
        });
    }
});