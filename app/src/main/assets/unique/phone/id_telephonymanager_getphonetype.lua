function after(hook, param)
    local res = param:getResult()
    local index = param:tryGetArgumentInt(0, 0)
    local settingIndex = index + 1;
    local nmeName = "cell.phone.type." .. tostring(settingIndex)
    if param:isForceSetting(nmeName, res) then
        local fake = param:getSettingInt(nmeName, 1)
        param:setResult(fake)
        return true, param:safe(res), param:safe(fake)
    end
    return false
end