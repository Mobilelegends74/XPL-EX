function after(hook, param)
    local ths = param:getThis()
    if ths == nil then
        return false
    end

    local res = false
    local name = param:getSetting("account.user.name")
    if name ~= nil and param:hasField(param:getThisClazz(), "name") then
        ths.name = name
        res = true
    end

    local serial = param:getSetting("account.user.serial")
    if serial ~= nil and param:hasField(param:getThisClazz(), "serialNumber") then
        if param:isNumericString(serial) then
            ths.serialNumber = tonumber(serial)
            res = true
        end
    end

    local gId = param:getSetting("account.user.group.id")
    if gId ~= nil and param:hasField(param:getThisClazz(), "profileGroupId") then
        if param:isNumericString(gId) then
            ths.profileGroupId = tonumber(gId)
            res = true
        end
    end

    local id = param:getSetting("account.user.id")
    if id ~= nil and param:hasField(param:getThisClazz(), "id") then
        if param:isNumericString(id) then
            ths.id = tonumber(id)
            res = true
        end
    end

    return res
end