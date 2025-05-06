function after(hook, param)
    local res = param:getResult()
    for i = 1, 2 do
        local nmeName = "cell.sim.subscription.id." .. i
        if param:isForceSetting(nmeName, res) then
            local fake = param:getSetting(nmeName)
            if  param:isNumericString(fake) then
                local num = tonumber(fake)
                param:setResult(num)
                return true, param:safe(res), param:safe(fake)
            end
        end
    end
    return false
end