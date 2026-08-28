function after(hook, param)
    local refreshRate = param:getSettingInt("display.refresh.rate.hz", 120)
    if refreshRate == nil or refreshRate < 60 or refreshRate > 240 then
        return false
    end

    local old = param:getResult()
    param:setResult(refreshRate + 0.0)
    return true, tostring(old), tostring(refreshRate)
end
