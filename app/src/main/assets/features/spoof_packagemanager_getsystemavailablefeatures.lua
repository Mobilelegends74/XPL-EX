function after(hook, param)
    -- Keep the complete platform feature list. Returning a one-item array
    -- makes hardware diagnostic apps report no camera, Wi-Fi or telephony.
    return false
end
