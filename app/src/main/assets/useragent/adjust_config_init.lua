function after(hook, param)
    local ths = param:getThis()
    if ths == nil then
        return false
    end

    if param:hasMethod(param:getThisClazz(), "setUserAgent") then
        local hooked = param:getValue('hooked', ths)
        if hooked then
            return false
        else
            param:putValue('hooked', true, ths)
        end

        local ua = param:getSetting("user.agent")
        hook(ths, 'setUserAgent', setUserAgent, ua)
        param:setUserAgent("dummy")
        return true
    end
end

function setUserAgent(when, param, ua)
    if when == 'before' then
        if param:getArgument(0) ~= ua then
            log('Setting ua=' .. ua)
            param:setArgument(0, ua)
        end
    end
end
