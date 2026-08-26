function before(hook, param)
    local old = param:getArgument(0)
    local agent = getSetting("user.agent")
    if agent == nil then
        return false
    end

    if old == nil then
        return false
    end

    param:setArgument(0, agent)
    return true, old, agent
end
