function after(hook, param)
    local res = param:getResult()
    for i = 1, 2 do
        local nmeName = "cell.unique.subscriber.id." .. i
        if param:isForceSetting(nmeName, res) then
            local fake = param:getSetting(nmeName)
            local shouldContinue = false

            if param:stringIsEmpty(fake) then
                local mcc = param:getSetting("cell.operator.mcc." .. i)
                local mnc = param:getSetting("cell.operator.mnc." .. i)
                local msn = param:getSetting("cell.unique.msin." .. i)
                if param:stringIsEmpty(mcc) or param:stringIsEmpty(mnc) or param:stringIsEmpty(msn) then
                    shouldContinue = true
                else
                    fake = mcc .. mnc .. msn
                end
            end

            if shouldContinue == false then
                param:setResult(fake)
                return true, param:safe(res), param:safe(fake)
            end
        end
    end
    local subName = "cell.unique.subscriber.id.1"
    if param:isForceSetting(subName, res) then
        local fake = param:getSetting(subName)
        param:setResult(fake)
        return true, param:safe(res), param:safe(fake)
    end
    return false
end