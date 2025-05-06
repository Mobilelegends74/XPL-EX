function after(hook, param)
    local res = param:getResult()
    for i = 1, 2 do
        local nmeName = "cell.unique.sim.icc.id." .. i
        if param:isForceSetting(nmeName, res) then
            local fake = param:getSetting(nmeName)
            param:setResult(fake)
            return true, param:safe(res), param:safe(fake)
        end
    end
    return false
end