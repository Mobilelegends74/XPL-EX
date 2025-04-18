function after(hook, param)
    local res = param:getResult()
    if res == nil then
        return false
    end

    local fake = param:getSettingBool("battery.is.power.save.mode.bool", false)
    param:setResult(fake)
    return true, param:safe(res), param:safe(fake)
end