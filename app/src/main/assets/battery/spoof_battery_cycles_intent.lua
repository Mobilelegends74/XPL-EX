function after(hook, param)
    local argOne = param:getArgument(0)
    if argOne == nil then
        return false
    end

    log("Intent=" .. argOne)
    if argOne ~= 'android.os.extra.CYCLE_COUNT' then
        return false
    end

    local res = param:getResult()
    if res == nil then
        return false
    end

    local fake = param:getSettingInt("battery.charging.cycles", 1)
	param:setResult(fake)
	return true, tostring(res), tostring(fake)
end