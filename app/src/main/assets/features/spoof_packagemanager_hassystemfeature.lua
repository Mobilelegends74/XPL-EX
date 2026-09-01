function after(hook, param)
    -- A device-profile spoof must never erase real hardware capabilities.
    -- Profiles do not currently carry a verified per-model feature catalog,
    -- so preserve PackageManager's original answer.
    return false
end
