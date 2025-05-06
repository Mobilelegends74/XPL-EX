function after(hook, param)
    local res = param:getResult()

    local index = param:tryGetArgumentInt(0, 0)
    local settingIndex = index + 1;
    local flag = param:getSettingInt("cell.phone.type." .. tostring(settingIndex), 1)
    local nmeName = "cell.unique.imei." .. tostring(settingIndex)
    if flag == 2 then
        nmeName = "cell.unique.meid." ..  tostring(settingIndex)
    end

    if param:isForceSetting(nmeName, res) then
        local fake = param:getSetting(nmeName)
        param:setResult(fake)
        return true, param:safe(res), param:safe(fake)
    end
    return false
end