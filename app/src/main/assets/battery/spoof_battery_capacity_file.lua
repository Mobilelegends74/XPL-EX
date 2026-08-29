function before(hook, param)
    local path = param:getArgument(0)
    if path == nil then
        return false
    end

    -- Capacity in percent is exposed by a different sysfs node. Only replace
    -- full/design charge values whose unit is microampere-hours.
    if not string.match(path, "/power_supply/[^/]+/charge_full$")
            and not string.match(path, "/power_supply/[^/]+/charge_full_design$") then
        return false
    end

    local fake = param:createFakeBatteryCapacityFile()
    if fake == nil or fake:getPath() == nil then
        return false
    end

    param:setArgumentString(0, fake:getPath())
    return true, path, fake:getPath(), "Battery design capacity"
end
